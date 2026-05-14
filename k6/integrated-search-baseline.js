/**
 * [Baseline] GET /api/v1/search — SearchService 성능 측정
 *
 * 목적: ES 도입 전 현재 성능을 수치로 기록한다.
 * 이 결과가 ES 도입 후 비교 기준(Before)이 된다.
 *
 * ※ 알라딘 API 총 호출: ~14건 (차단 방지용 보수 설정)
 *   SearchService에 Redis 캐시 게이트(isAladinQuerySynced) 존재 —
 *   키워드별 첫 번째 호출만 Aladin API 호출, 이후는 DB 직접 조회
 *   기존 설정은 시나리오3 20VU×30s → 수백건 (차단 원인)
 *   - 기존: 시나리오3 20VU×30s, sleep0.3s → ~2000 HTTP 요청
 *   - 변경: 시나리오3 3VU×20s, sleep3s → ~20 HTTP 요청
 *
 * 사전 준비:
 * 1. 서버 실행 확인: http://localhost:8080/actuator/health
 * 2. (선택) 로그인 시나리오 포함 시: k6 run k6/setup-users.js 로 토큰 생성
 *
 * 실행:
 * k6 run k6/integrated-search-baseline.js
 * k6 run -e BASE_URL=http://54.180.101.81:8080 k6/integrated-search-baseline.js
 *
 * 결과 저장:
 * k6 run --out json=k6/results/integrated-search-before-es.json k6/integrated-search-baseline.js
 */

import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Trend } from 'k6/metrics';
import { SharedArray } from 'k6/data';
import exec from 'k6/execution';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

let tokens = [];
try {
  tokens = new SharedArray('tokens', function () {
    return JSON.parse(open('./tokens.local.json'));
  });
} catch (_) {}

function getToken(vuIndex) {
  return tokens.length > 0 ? tokens[vuIndex % tokens.length] : null;
}
function authHeader(token) {
  return token ? { Authorization: `Bearer ${token}` } : {};
}

const cacheMissDuration   = new Trend('integrated_cache_miss_duration', true);
const cacheHitDuration    = new Trend('integrated_cache_hit_duration', true);
const typeCompDuration    = new Trend('integrated_type_comparison_duration', true);
const concurrentDuration  = new Trend('integrated_concurrent_duration', true);
const paginationDuration  = new Trend('integrated_pagination_duration', true);
const searchErrorRate     = new Rate('integrated_search_error_rate');

// ── 테스트 시나리오 ───────────────────────────────────────────────────────
// ※ 알라딘 API 차단 방지: 전체 호출 ~14건, Redis 캐시 히트 이후는 0건
export const options = {
  summaryTrendStats: ['avg', 'min', 'med', 'max', 'p(90)', 'p(95)', 'p(99)'],
  scenarios: {
    // 시나리오 1: Redis 캐시 미스 vs 히트 비교 — Aladin 최대 5건
    cache_miss_hit: {
      executor: 'per-vu-iterations',
      vus: 1,
      iterations: 5,
      startTime: '0s',
    },
    // 시나리오 2: type별 응답시간 비교 (all/book/user) — 캐시 히트, Aladin ~0건
    type_comparison: {
      executor: 'per-vu-iterations',
      vus: 3,
      iterations: 5,
      startTime: '40s',
    },
    // 시나리오 3: 동시 사용자 부하 — 3VU×20s/sleep3s ≈ 20 HTTP, Aladin ~3건
    concurrent_load: {
      executor: 'constant-vus',
      vus: 3,
      duration: '20s',
      startTime: '70s',
    },
    // 시나리오 4: cursor 페이지네이션 — Aladin ~1건
    cursor_pagination: {
      executor: 'per-vu-iterations',
      vus: 1,
      iterations: 3,
      startTime: '100s',
    },
  },
  thresholds: {
    http_req_failed:              ['rate<0.10'],
    integrated_search_error_rate: ['rate<0.10'],
  },
};

// 시나리오 1: 각 이터레이션마다 다른 키워드로 캐시 미스 유도
const MISS_KEYWORDS = ['채식주의자', '82년생 김지영', '아몬드', '코스모스', '총균쇠'];
// 이후 시나리오: 캐시 히트용 (이미 1번씩 호출된 키워드)
const HIT_KEYWORDS  = ['채식주의자', '82년생 김지영', '아몬드', '코스모스', '총균쇠'];
const LOAD_KEYWORDS = ['소설', '자기계발', '역사', '과학', '철학'];
const PAGING_QUERY  = '한국 소설';

export default function () {
  const scenario = exec.scenario.name;
  const token    = getToken(exec.vu.idInTest - 1);

  if      (scenario === 'cache_miss_hit')  runCacheMissHit(token);
  else if (scenario === 'type_comparison') runTypeComparison(token);
  else if (scenario === 'concurrent_load') runConcurrentLoad(token);
  else if (scenario === 'cursor_pagination') runCursorPagination(token);
}

function runCacheMissHit(token) {
  const vuIter = exec.scenario.iterationInTest;
  const keyword = MISS_KEYWORDS[vuIter % MISS_KEYWORDS.length];

  // 첫 번째 호출 — 캐시 미스, Aladin API 호출 발생
  const url = `${BASE_URL}/api/v1/search?query=${encodeURIComponent(keyword)}&type=book&limit=10`;
  let start = Date.now();
  let res = http.get(url, { headers: authHeader(token) });
  let elapsed = Date.now() - start;
  cacheMissDuration.add(elapsed);

  const ok = check(res, {
    '[miss] 상태코드 200': (r) => r.status === 200,
    '[miss] 응답 구조 정상': (r) => {
      try { return JSON.parse(r.body).status === 'SUCCESS'; }
      catch { return false; }
    },
  });
  if (!ok) {
    searchErrorRate.add(1);
    console.error(`[캐시미스] 실패 "${keyword}" status=${res.status} ${elapsed}ms`);
  } else {
    searchErrorRate.add(0);
    console.log(`[캐시미스] "${keyword}" | ${elapsed}ms`);
  }

  sleep(3);

  // 두 번째 호출 — 캐시 히트, DB만 조회
  start = Date.now();
  res = http.get(url, { headers: authHeader(token) });
  elapsed = Date.now() - start;
  cacheHitDuration.add(elapsed);

  check(res, { '[hit] 상태코드 200': (r) => r.status === 200 });
  console.log(`[캐시히트] "${keyword}" | ${elapsed}ms`);

  sleep(3);
}

function runTypeComparison(token) {
  const types = ['all', 'book', 'user'];
  const keyword = HIT_KEYWORDS[Math.floor(Math.random() * HIT_KEYWORDS.length)];

  for (const type of types) {
    const url = `${BASE_URL}/api/v1/search?query=${encodeURIComponent(keyword)}&type=${type}&limit=10`;
    const start = Date.now();
    const res = http.get(url, { headers: authHeader(token) });
    const elapsed = Date.now() - start;

    typeCompDuration.add(elapsed);
    check(res, { [`[${type}] 상태코드 200`]: (r) => r.status === 200 });
    console.log(`[타입비교] type=${type} "${keyword}" | ${elapsed}ms`);
    sleep(2);
  }
}

function runConcurrentLoad(token) {
  const keyword = LOAD_KEYWORDS[Math.floor(Math.random() * LOAD_KEYWORDS.length)];
  const url = `${BASE_URL}/api/v1/search?query=${encodeURIComponent(keyword)}&type=book&limit=10`;

  const start = Date.now();
  const res = http.get(url, { headers: authHeader(token) });
  const elapsed = Date.now() - start;

  concurrentDuration.add(elapsed);
  check(res, { '상태코드 200': (r) => r.status === 200 });

  if (res.status !== 200) {
    searchErrorRate.add(1);
    console.error(`[동시부하] 실패 "${keyword}" status=${res.status} ${elapsed}ms`);
  } else {
    searchErrorRate.add(0);
    console.log(`[동시부하] "${keyword}" | ${elapsed}ms`);
  }
  sleep(3);
}

function runCursorPagination(token) {
  const url1 = `${BASE_URL}/api/v1/search?query=${encodeURIComponent(PAGING_QUERY)}&type=book&limit=10`;
  let start = Date.now();
  let res = http.get(url1, { headers: authHeader(token) });
  let elapsed = Date.now() - start;
  paginationDuration.add(elapsed);

  let nextCursor = null;
  check(res, {
    '[page1] 상태코드 200': (r) => r.status === 200,
    '[page1] 응답 정상': (r) => {
      try {
        const data = JSON.parse(r.body).data;
        nextCursor = data?.nextCursor ?? null;
        return data !== undefined;
      } catch { return false; }
    },
  });
  console.log(`[페이지네이션] page1 "${PAGING_QUERY}" | ${elapsed}ms`);
  sleep(2);

  if (!nextCursor) {
    console.warn(`[페이지네이션] nextCursor 없음 — page2 스킵 (query="${PAGING_QUERY}")`);
    return;
  }

  const url2 = `${BASE_URL}/api/v1/search?query=${encodeURIComponent(PAGING_QUERY)}&type=book&limit=10&cursor=${nextCursor}`;
  start = Date.now();
  res = http.get(url2, { headers: authHeader(token) });
  elapsed = Date.now() - start;
  paginationDuration.add(elapsed);

  check(res, { '[page2] 상태코드 200': (r) => r.status === 200 });
  console.log(`[페이지네이션] page2 | ${elapsed}ms`);
  sleep(2);
}

export function handleSummary(data) {
  const m = data.metrics;
  const fmt = (key, sub) => {
    const val = m[key]?.values?.[sub];
    return val != null ? `${Math.round(val)}ms` : 'N/A';
  };
  const errRate = ((m['integrated_search_error_rate']?.values?.rate ?? 0) * 100).toFixed(1) + '%';

  console.log(`
╔════════════════════════════════════════════════════════════════╗
║  [Before ES] SearchService 검색 성능 기준선                    ║
║  (실제 알라딘 API — 총 ~14건, Redis 캐시 게이트 포함)         ║
╠════════════════════════════════════════════════════════════════╣
║ 캐시 미스 응답시간 (integrated_cache_miss_duration)            ║
║   P50 : ${fmt('integrated_cache_miss_duration','med').padEnd(8)}                       ║
║   P95 : ${fmt('integrated_cache_miss_duration','p(95)').padEnd(8)}                     ║
╠════════════════════════════════════════════════════════════════╣
║ 캐시 히트 응답시간 (integrated_cache_hit_duration)             ║
║   P50 : ${fmt('integrated_cache_hit_duration','med').padEnd(8)}                        ║
║   P95 : ${fmt('integrated_cache_hit_duration','p(95)').padEnd(8)}                      ║
╠════════════════════════════════════════════════════════════════╣
║ type별 비교 (integrated_type_comparison_duration)              ║
║   P95 : ${fmt('integrated_type_comparison_duration','p(95)').padEnd(8)}                ║
╠════════════════════════════════════════════════════════════════╣
║ 동시 부하 (integrated_concurrent_duration)                     ║
║   P95 : ${fmt('integrated_concurrent_duration','p(95)').padEnd(8)}                     ║
╠════════════════════════════════════════════════════════════════╣
║ 페이지네이션 (integrated_pagination_duration)                  ║
║   P95 : ${fmt('integrated_pagination_duration','p(95)').padEnd(8)}                     ║
╠════════════════════════════════════════════════════════════════╣
║ 오류율: ${errRate.padEnd(8)}                                   ║
╠════════════════════════════════════════════════════════════════╣
║ ES 도입 후 동일하게 재실행 → before/after 비교 가능           ║
╚════════════════════════════════════════════════════════════════╝
`);
  return {};
}

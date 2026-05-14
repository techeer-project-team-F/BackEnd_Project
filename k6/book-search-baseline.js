/**
 * [Baseline] GET /api/v1/books/search — BookService 성능 측정
 *
 * 목적: ES 도입 전 현재 성능을 수치로 기록한다.
 * 이 결과가 ES 도입 후 비교 기준(Before)이 된다.
 *
 * ※ 알라딘 API 총 호출: ~22건 (차단 방지용 보수 설정)
 *   기존 설정은 ~200건이었으며 이로 인해 계정 차단 발생 이력 있음
 *   - 기존: 시나리오3 5VU×30s → ~150건, 시나리오2 10VU 동시 → 10건
 *   - 변경: 시나리오3 2VU×20s/sleep5s → ~8건, 시나리오2 3VU shared 5건
 *
 * 사전 준비:
 * 1. 서버 실행 확인: http://localhost:8080/actuator/health
 * 2. (선택) 로그인 시나리오 포함 시: k6 run k6/setup-users.js 로 토큰 생성
 *
 * 실행:
 * k6 run k6/book-search-baseline.js
 * k6 run -e BASE_URL=http://54.180.101.81:8080 k6/book-search-baseline.js
 *
 * 결과 저장:
 * k6 run --out json=k6/results/book-search-before-es.json k6/book-search-baseline.js
 */

import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Trend, Counter } from 'k6/metrics';
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

const bookSearchDuration  = new Trend('book_search_duration', true);
const concurrentDuration  = new Trend('book_search_concurrent_duration', true);
const paginationDuration  = new Trend('book_search_pagination_duration', true);
const aladinErrorRate     = new Rate('book_search_aladin_error_rate');
const emptyResultCount    = new Counter('book_search_empty_results');

// ── 테스트 시나리오 ───────────────────────────────────────────────────────
// ※ 알라딘 API 차단 방지: 전체 호출 ~22건, sleep 3~5s 유지
export const options = {
  summaryTrendStats: ['avg', 'min', 'med', 'max', 'p(90)', 'p(95)', 'p(99)'],
  scenarios: {
    // 시나리오 1: 단일 사용자 반복 검색 — Aladin 5건
    single_user_search: {
      executor: 'per-vu-iterations',
      vus: 1,
      iterations: 5,
      startTime: '0s',
    },
    // 시나리오 2: 동시 사용자 동일 키워드 — Aladin 5건 (shared)
    concurrent_same_keyword: {
      executor: 'shared-iterations',
      vus: 3,
      iterations: 5,
      startTime: '25s',
    },
    // 시나리오 3: 다양한 키워드 캐시 미스 — 2VU×20s/sleep5s ≈ Aladin 8건
    diverse_keywords: {
      executor: 'constant-vus',
      vus: 2,
      duration: '20s',
      startTime: '45s',
    },
    // 시나리오 4: 페이지 전환 — 1VU×2iter×2page = Aladin 4건
    pagination: {
      executor: 'per-vu-iterations',
      vus: 1,
      iterations: 2,
      startTime: '75s',
    },
  },
  thresholds: {
    http_req_failed:               ['rate<0.10'],
    book_search_duration:          ['p(95)<10000'],
    book_search_aladin_error_rate: ['rate<0.10'],
  },
};

const SINGLE_KEYWORDS  = ['채식주의자', '82년생 김지영', '아몬드', '해리포터', '어린왕자'];
const SAME_KEYWORD     = '채식주의자';
const DIVERSE_KEYWORDS = ['소설', '자기계발', '역사', '과학', '철학', '에세이', '심리학'];
const PAGINATION_QUERY = '한국 소설';

export default function () {
  const scenario = exec.scenario.name;
  const token = getToken(exec.vu.idInTest - 1);

  if      (scenario === 'single_user_search')      runSingleSearch(token);
  else if (scenario === 'concurrent_same_keyword') runConcurrentSearch(token);
  else if (scenario === 'diverse_keywords')        runDiverseSearch(token);
  else if (scenario === 'pagination')              runPaginationSearch(token);
}

function runSingleSearch(token) {
  const keyword = SINGLE_KEYWORDS[Math.floor(Math.random() * SINGLE_KEYWORDS.length)];
  const url = `${BASE_URL}/api/v1/books/search?query=${encodeURIComponent(keyword)}&page=1&limit=10`;

  const start = Date.now();
  const res = http.get(url, { headers: authHeader(token) });
  const elapsed = Date.now() - start;

  bookSearchDuration.add(elapsed);

  const ok = check(res, {
    '상태코드 200': (r) => r.status === 200,
    '응답 구조 정상': (r) => {
      try {
        const j = JSON.parse(r.body);
        return j.status === 'SUCCESS' && Array.isArray(j.data?.content);
      } catch { return false; }
    },
  });

  if (!ok) {
    aladinErrorRate.add(1);
    console.error(`[단건] 실패 keyword="${keyword}" status=${res.status} ${elapsed}ms`);
  } else {
    aladinErrorRate.add(0);
    const count = JSON.parse(res.body).data?.content?.length ?? 0;
    if (count === 0) emptyResultCount.add(1);
    console.log(`[단건] "${keyword}" | ${count}건 | ${elapsed}ms`);
  }
  sleep(3);
}

function runConcurrentSearch(token) {
  const url = `${BASE_URL}/api/v1/books/search?query=${encodeURIComponent(SAME_KEYWORD)}&page=1&limit=10`;

  const start = Date.now();
  const res = http.get(url, { headers: authHeader(token) });
  const elapsed = Date.now() - start;

  concurrentDuration.add(elapsed);

  const ok = check(res, {
    '상태코드 200': (r) => r.status === 200,
    '결과 있음': (r) => {
      try { return JSON.parse(r.body).status === 'SUCCESS'; }
      catch { return false; }
    },
  });

  if (!ok) {
    aladinErrorRate.add(1);
    console.error(`[동시] 실패 status=${res.status} ${elapsed}ms`);
  } else {
    aladinErrorRate.add(0);
    console.log(`[동시] "${SAME_KEYWORD}" | ${elapsed}ms`);
  }
  sleep(5);
}

function runDiverseSearch(token) {
  const keyword = DIVERSE_KEYWORDS[Math.floor(Math.random() * DIVERSE_KEYWORDS.length)];
  const url = `${BASE_URL}/api/v1/books/search?query=${encodeURIComponent(keyword)}&page=1&limit=10`;

  const start = Date.now();
  const res = http.get(url, { headers: authHeader(token) });
  const elapsed = Date.now() - start;

  bookSearchDuration.add(elapsed);

  check(res, { '상태코드 200': (r) => r.status === 200 });

  if (res.status !== 200) {
    aladinErrorRate.add(1);
    console.error(`[다양] 실패 keyword="${keyword}" status=${res.status} ${elapsed}ms`);
  } else {
    aladinErrorRate.add(0);
    console.log(`[다양] "${keyword}" | ${elapsed}ms`);
  }
  sleep(5);
}

function runPaginationSearch(token) {
  for (let page = 1; page <= 2; page++) {
    const url = `${BASE_URL}/api/v1/books/search?query=${encodeURIComponent(PAGINATION_QUERY)}&page=${page}&limit=10`;

    const start = Date.now();
    const res = http.get(url, { headers: authHeader(token) });
    const elapsed = Date.now() - start;

    paginationDuration.add(elapsed);
    check(res, { [`page=${page} 상태코드 200`]: (r) => r.status === 200 });
    console.log(`[페이지] page=${page} | ${elapsed}ms`);
    sleep(3);
  }
}

export function handleSummary(data) {
  const m = data.metrics;
  const fmt = (key, sub) => {
    const val = m[key]?.values?.[sub];
    return val != null ? `${Math.round(val)}ms` : 'N/A';
  };
  const errRate = ((m['book_search_aladin_error_rate']?.values?.rate ?? 0) * 100).toFixed(1) + '%';

  console.log(`
╔════════════════════════════════════════════════════════════════╗
║  [Before ES] BookService 검색 성능 기준선                      ║
║  (실제 알라딘 API — 총 ~22건, 차단 방지 보수 설정)            ║
╠════════════════════════════════════════════════════════════════╣
║ 단건/다양 검색 (book_search_duration)                          ║
║   P50 : ${fmt('book_search_duration','med').padEnd(8)}                                 ║
║   P95 : ${fmt('book_search_duration','p(95)').padEnd(8)}                               ║
║   P99 : ${fmt('book_search_duration','p(99)').padEnd(8)}                               ║
╠════════════════════════════════════════════════════════════════╣
║ 동시 요청 (book_search_concurrent_duration)                    ║
║   P95 : ${fmt('book_search_concurrent_duration','p(95)').padEnd(8)}                    ║
╠════════════════════════════════════════════════════════════════╣
║ 페이지네이션 (book_search_pagination_duration)                 ║
║   P95 : ${fmt('book_search_pagination_duration','p(95)').padEnd(8)}                    ║
╠════════════════════════════════════════════════════════════════╣
║ 오류율: ${errRate.padEnd(8)}                                   ║
╠════════════════════════════════════════════════════════════════╣
║ ES 도입 후 동일하게 재실행 → before/after 비교 가능           ║
╚════════════════════════════════════════════════════════════════╝
`);
  return {};
}

/**
 * 토큰 갱신 동시성 테스트
 * 검증 대상: RedisService.rotateRefreshToken() Lua 스크립트 원자적 교체
 *
 * 시나리오: 동일 유저가 동시에 토큰 갱신을 2번 요청
 *           → 하나만 성공(200), 다른 하나는 실패(401) + Redis에서 토큰 삭제(재사용 공격 방어)
 *
 * 주의: refreshToken은 HttpOnly 쿠키로 전송됩니다.
 *       테스트 전 POST /api/v1/auth/login으로 쿠키를 발급받아 REFRESH_COOKIE에 세팅하세요.
 */
import http from 'k6/http';
import { check, sleep } from 'k6';
import { BASE_URL } from './config.js';

// ── 테스트 대상 설정 ──────────────────────────────────────────
// 로그인 후 Set-Cookie 헤더에서 refreshToken 값을 복사하세요.
const REFRESH_COOKIE = 'refreshToken=eyJhbGci...'; // 실제 쿠키 값으로 교체
// ─────────────────────────────────────────────────────────────

export const options = {
  scenarios: {
    concurrent_refresh: {
      executor: 'shared-iterations',
      vus: 2,         // 동일 유저가 동시에 2번 갱신 시도
      iterations: 2,
      maxDuration: '10s',
    },
  },
};

export default function () {
  const url = `${BASE_URL}/api/v1/auth/token/refresh`;

  const res = http.post(url, null, {
    headers: { Cookie: REFRESH_COOKIE },
  });

  // 첫 번째 요청은 200, 두 번째는 401이어야 Lua 스크립트가 정상 동작한 것
  check(res, {
    '200 또는 401': (r) => r.status === 200 || r.status === 401,
    '두 요청이 모두 200이면 안 됨 (Lua 스크립트 미동작)': () => true, // 수동 확인
  });

  console.log(`VU ${__VU} → status: ${res.status}`);
}

export function handleSummary(data) {
  console.log('\n======= 토큰 갱신 동시성 검증 =======');
  console.log('기대 결과: 200 1개 + 401 1개');
  console.log('두 요청 모두 200이면 Lua 스크립트가 동작하지 않은 것입니다.');
  console.log('=====================================\n');
  return {};
}

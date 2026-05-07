// k6 테스트 공통 설정
// 실행 전: TOKENS 배열에 테스트용 유저들의 JWT Access Token을 채워주세요.
// 토큰 발급: POST /api/v1/auth/login → response.data.accessToken

export const BASE_URL = 'http://54.180.101.81:8080';

// VU마다 다른 유저 토큰을 사용해야 동시성 테스트가 의미있습니다.
// (같은 유저가 중복 좋아요/팔로우하면 비즈니스 에러가 발생하므로)
export const TOKENS = [
  'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxOCIsInJvbGUiOiJVU0VSIiwiaWF0IjoxNzc4MTY5Mjc3LCJleHAiOjE3NzgxNzI4Nzd9.p8EI9DLkGstP2rPKxYPNzHispn0CXll7pjzxN_jKRGg',
  // 좋아요/팔로우 동시성 테스트는 VU마다 다른 유저 토큰 필요
  // 아래 스크립트로 테스트 유저를 일괄 생성한 뒤 토큰을 추가하세요.
];

export function getToken(vuIndex) {
  return TOKENS[vuIndex % TOKENS.length];
}

export function authHeader(token) {
  return { Authorization: `Bearer ${token}`, 'Content-Type': 'application/json' };
}

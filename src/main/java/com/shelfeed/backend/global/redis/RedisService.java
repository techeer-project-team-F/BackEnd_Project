package com.shelfeed.backend.global.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisService {
    private final StringRedisTemplate redisTemplate;
    // 데이터가 섞이지 않도록 하는 레디스의 규칙(Redis 암묵적인 룰 )
    private static final String REFRESH_PREFIX     = "auth:refresh:";
    private static final String BLACKLIST_PREFIX   = "auth:blacklist:";
    private static final String EMAIL_CODE_PREFIX  = "auth:email:code:";
    private static final String EMAIL_ATTEMPTS_PREFIX = "auth:email:attempts:";
    private static final String EMAIL_COOLDOWN_PREFIX = "auth:email:cooldown:";
    private static final String PW_RESET_PREFIX    = "auth:pwreset:";
    private static final String OAUTH_STATE_PREFIX = "auth:oauth:state:";
    private static final String MEMBER_SEQ_KEY     = "seq:member";

    // Refresh Token : JWT는 서버 강제 무효화 불가하기에 로그인,갱신,로그아웃 시 저장·검증·삭제 형식으로 만들기
    public void saveRefreshToken(Long memberUserId, String refreshToken, long ttlSeconds){//opsForValue: Key-Value로 사용할 것을 정의
        redisTemplate.opsForValue().set(
                REFRESH_PREFIX + memberUserId, refreshToken, ttlSeconds, TimeUnit.SECONDS);
    }
    public String getRefreshToken(Long memberUserId){
        return redisTemplate.opsForValue().get(REFRESH_PREFIX + memberUserId);
    }
    public void deleteRefreshToken(Long memberUserId){
         redisTemplate.delete(REFRESH_PREFIX + memberUserId);
    }

    // Access Token 블랙리스트: 로그아웃 후 만료 전 토큰 재사용 방지
    public void addToBlacklist(String accessToken, long remainingMs){
        redisTemplate.opsForValue().set( // remainingMs 시간 만큼 못쓰게 하면서 시간 지나면 삭제 하도록
                BLACKLIST_PREFIX + accessToken,"1", remainingMs, TimeUnit.SECONDS);
    }
    public boolean isBlacklisted(String accessToken){
        return redisTemplate.hasKey(BLACKLIST_PREFIX + accessToken);
    }

    //이메일 인증 코드: TTL 5분, 시도 횟수 관리 (5회 초과 시 코드 삭제)
    public void saveEmailCode(String email, String code, long ttlSeconds){
        redisTemplate.opsForValue().set(
                EMAIL_CODE_PREFIX + email, code, ttlSeconds, TimeUnit.SECONDS);
        redisTemplate.delete(EMAIL_ATTEMPTS_PREFIX + email); // 이전 재시도 횟수를 없애기
    }
    public String getEmailCode(String email){
        return redisTemplate.opsForValue().get(EMAIL_CODE_PREFIX + email);
    }
    public void deleteEmailCode(String email) {// 인증 완료 되면 사용하는 용도
        redisTemplate.delete(EMAIL_CODE_PREFIX + email);
        redisTemplate.delete(EMAIL_ATTEMPTS_PREFIX + email);
    }
    public long incrementEmailVerifyAttempts(String email){
        Long count = redisTemplate.opsForValue().increment(EMAIL_ATTEMPTS_PREFIX + email);
        redisTemplate.expire(EMAIL_ATTEMPTS_PREFIX + email, 5, TimeUnit.MINUTES);
        return count == null ? 1 : count;
    }

    //재발송 쿨다운: 무한 재시도 막기 위함
    public boolean setResendCooldown(String email, long ttlSeconds) {
        return Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(EMAIL_COOLDOWN_PREFIX + email,"1",ttlSeconds,TimeUnit.SECONDS));
    }

    //비밀번호 재설정 토큰: UUID, TTL 30분
    public void savePasswordResetToken(String token, String email, long ttlSeconds) {
        redisTemplate.opsForValue().set(PW_RESET_PREFIX + token, email, ttlSeconds, TimeUnit.SECONDS);
    }
    public String getEmailByPasswordResetToken(String token) {
        return redisTemplate.opsForValue().get(PW_RESET_PREFIX + token);
    }
    public void deletePasswordResetToken(String token) {redisTemplate.delete(PW_RESET_PREFIX + token);
    }

    //Google auth state
    public void saveOAuthState(String state, long ttlSeconds) {
        redisTemplate.opsForValue().set(
                OAUTH_STATE_PREFIX + state, "1", ttlSeconds, TimeUnit.SECONDS);
    }

    // 검증 성공 시 즉시 삭제(일회용) → true, 없으면 → false
    public boolean validateAndDeleteOAuthState(String state) {
        String key = OAUTH_STATE_PREFIX + state;
        if (redisTemplate.hasKey(key)) {
            redisTemplate.delete(key);
            return true;
        }
        return false;
    }

    //memberUserId 시퀀스: INCR 명령으로 동시성 없는 증가 ID 생성
    public Long generateMemberUserId() {
        return redisTemplate.opsForValue().increment(MEMBER_SEQ_KEY);
    }
}

package com.shelfeed.backend.global.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisService {
    private final StringRedisTemplate redisTemplate;
    // 데이터가 섞이지 않도록 하는 레디스의 규칙
    private static final String REFRESH_PREFIX     = "auth:refresh:";
    private static final String BLACKLIST_PREFIX   = "auth:blacklist:";
    private static final String EMAIL_CODE_PREFIX  = "auth:email:code:";
    private static final String EMAIL_ATTEMPTS_PREFIX = "auth:email:attempts:";
    private static final String EMAIL_COOLDOWN_PREFIX = "auth:email:cooldown:";
    private static final String PW_RESET_PREFIX    = "auth:pwreset:";
    private static final String OAUTH_STATE_PREFIX = "auth:oauth:state:";
    private static final String MEMBER_SEQ_KEY     = "seq:member";


}

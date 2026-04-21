package com.shelfeed.backend.global.email;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmailServiceImpl implements EmailService {

    @Override
    public void sendVerificationEmail(String email, String code) {
        // TODO: SMTP 설정 후 실제 메일 발송으로 교체
        log.info("[EMAIL] 인증 코드 발송 → to={}, code={}", email, code);
    }

    @Override
    public void sendPasswordResetEmail(String email, String token) {
        // TODO: SMTP 설정 후 실제 메일 발송으로 교체
        log.info("[EMAIL] 비밀번호 재설정 링크 발송 → to={}", email);
    }
}

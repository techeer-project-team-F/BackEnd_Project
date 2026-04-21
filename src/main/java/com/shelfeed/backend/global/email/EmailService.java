package com.shelfeed.backend.global.email;

public interface EmailService {
    void sendVerificationEmail(String email, String code);
    void sendPasswordResetEmail(String email, String token);
}

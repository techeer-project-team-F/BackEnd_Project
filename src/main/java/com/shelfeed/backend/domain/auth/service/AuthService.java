package com.shelfeed.backend.domain.auth.service;

import com.shelfeed.backend.domain.auth.dto.request.*;
import com.shelfeed.backend.domain.auth.dto.response.*;
import com.shelfeed.backend.domain.member.entity.Member;
import com.shelfeed.backend.domain.member.enums.MemberStatus;
import com.shelfeed.backend.domain.member.repository.MemberRepository;
import com.shelfeed.backend.domain.member.repository.SocialAccountRepository;
import com.shelfeed.backend.global.common.exception.BusinessException;
import com.shelfeed.backend.global.common.exception.ErrorCode;
import com.shelfeed.backend.global.jwt.JwtProvider;
import com.shelfeed.backend.global.redis.RedisService;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {
    private final MemberRepository memberRepository;
    private final SocialAccountRepository socialAccountRepository;
    private final JwtProvider jwtProvider;
    private final RedisService redisService;
    private final PasswordEncoder passwordEncoder;

    @Value("${oauth2.google.client-id}")
    private String googleClientId;
    @Value("${oauth2.google.redirect-uri}")
    private  String googleRedirectUri;

    private static final int MAX_EMAIL_VERIFY_ATTEMPTS = 5;

    // ── 1. 이메일 회원가입
    public  TokenPair signup(SignupRequest request){
        if (memberRepository.existsByEmail(request.getEmail())){//이메일 존재 시 예외 펑
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
        if (memberRepository.existsByNickname(request.getNickname())){//닉넴 중복 시 예외 펑
            throw new BusinessException(ErrorCode.NICKNAME_ALREADY_EXISTS);
        }

        Long memberUserId = redisService.generateMemberUserId(); //중복되지 않게 redis에서 부여한 ID

        Member member = Member.createLocal(memberUserId, request.getEmail(), passwordEncoder.encode(request.getPassword()), request.getNickname());

        memberRepository.save(member); // 저장

        String accessToken = jwtProvider.generateAccessToken(member); //인증 토큰
        String refreshToken = jwtProvider.generateRefreshToken(member); // 재발급 토큰
        redisService.saveRefreshToken(memberUserId, refreshToken, jwtProvider.getRefreshTokenExpiresIn());// 재발급 토큰 저장

        return new TokenPair(SignupResponse.of(member, accessToken, jwtProvider.getAccessTokenExpiresIn()), refreshToken
        );
    }
    // ── 2. 이메일 인증 확인
    public EmailVerifyResponse verifyEmail(EmailVerifyRequest request){
        long attempts = redisService.incrementEmailVerifyAttempts(request.getEmail());
        if (attempts > MAX_EMAIL_VERIFY_ATTEMPTS){
            redisService.deleteEmailCode(request.getEmail()); //5번 이상 넘어가면 삭제
            throw new BusinessException(ErrorCode.CODE_ATTEMPTS_EXCEEDED);
        }

        String storedCode = redisService.getEmailCode(request.getEmail());
        if (storedCode == null){throw new BusinessException(ErrorCode.CODE_EXPIRED);} //만료 or 없으면 예외 펑

        if(!storedCode.equals(request.getCode())){
            throw new BusinessException(ErrorCode.INVALID_EMAIL_CODE);//이메일 코드 다르면 펑
        }

        Member member =  memberRepository.findByEmail(request.getEmail())
                .orElseThrow(()-> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));// 없는 이메일 이면 펑
        member.verifyEmail();
        redisService.deleteEmailCode(request.getEmail());// 인증 완료 했으니까 지우기

        return EmailVerifyResponse.of(request.getEmail(), true);
    }

    // ── 3. 이메일 인증 코드 재발송
    public void resendEmailCode(EmailResendRequest request){
        Member member = memberRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        if (member.isEmailVerified()){
            throw new BusinessException(ErrorCode.ALREADY_VERIFIED_EMAIL);//이미 인증이 되었으니까 재발송 필요 없
        }

        boolean cooldownSet = redisService.setResendCooldown(request.getEmail(),60); // 1분간 대기
        if (!cooldownSet){
            throw new BusinessException(ErrorCode.RESEND_COOLDOWN);
        }
        String code = generateSixDigitCode();
        redisService.saveEmailCode(request.getEmail(), code, 300);
        // emailService.sendVerificationEmail(request.getEmail(), code); 찐으로 메일 보내는 코드
    }

    // ── 4. 이메일 로그인
    public LoginTokenPair login(LoginRequest request){
        Member member = memberRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_PASSWORD));

        if (!passwordEncoder.matches(request.getPassword(),member.getPassword())){
            throw new BusinessException(ErrorCode.INVALID_PASSWORD);//페스워드 예외 처리
        }
        if (member.getStatus() == MemberStatus.WITHDRAWN){
            throw new BusinessException(ErrorCode.WITHDRAWN_MEMBER);//탈퇴인 예외 처리
        }

        member.recordLogin(); //로그인 시점 기록

        String accessToken = jwtProvider.generateAccessToken(member);
        String refreshToken = jwtProvider.generateRefreshToken(member);
        redisService.saveRefreshToken(member.getMemberUserId(), refreshToken, jwtProvider.getRefreshTokenExpiresIn());

        return new LoginTokenPair(
                LoginResponse.of(member, accessToken, jwtProvider.getAccessTokenExpiresIn()), refreshToken);
    }

    // ── 5. Google OAuth 로그인 URL 생성
    @Transactional(readOnly = true)
    public OAuthLoginUrlResponse getGoogleLoginUrl(){
        String state = UUID.randomUUID().toString();
        redisService.saveOAuthState(state, 300);//5분 유효

        String logUrl = UriComponentsBuilder
                .fromUriString("https://accounts.google.com/o/oauth2/v2/auth")
                .queryParam("client_id", googleClientId)
                .queryParam("redirect_uri", googleRedirectUri)
                .queryParam("response_type", "code")
                .queryParam("scope", "openid email profile")
                .queryParam("state", state)
                .build().toUriString();
        return OAuthLoginUrlResponse.of(logUrl);
    }

    // ── 6. Google OAuth 로그인 완료(구현 예정)

    // ── 7. 토큰 갱신
    public RefreshTokenPair refresh(String refreshToken){
        if (refreshToken == null){
            throw new BusinessException(ErrorCode.INVALID_TOKEN); //재발급 없으면 에러 펑
        }
        Long memberUserId;
        try{
            if (!jwtProvider.validateToken(refreshToken)){
                throw new BusinessException(ErrorCode.INVALID_TOKEN);//인증 안된 토큰 이면 에러 펑
            }
            memberUserId = jwtProvider.getMemberUserId(refreshToken);
        } catch (ExpiredJwtException e) {
            throw new BusinessException(ErrorCode.EXPIRED_TOKEN);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        String storedToken = redisService.getRefreshToken(memberUserId);

        if (storedToken == null || !storedToken.equals(refreshToken)) {//저장 된 것과 다르 거나 없으면 에러 펑
            redisService.deleteRefreshToken(memberUserId);
            throw new BusinessException(ErrorCode.TOKEN_REUSE_DETECTED);
        }

        Member member = memberRepository.findByMemberUserId(memberUserId)//우리 회원인지 한 번 더 확인
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        String newAccessToken = jwtProvider.generateAccessToken(member);
        String newRefreshToken = jwtProvider.generateRefreshToken(member);
        redisService.saveRefreshToken(memberUserId, newRefreshToken, jwtProvider.getRefreshTokenExpiresIn());

        return new RefreshTokenPair(
                TokenRefreshResponse.of(newAccessToken, jwtProvider.getAccessTokenExpiresIn()), newRefreshToken
        );
    }

    // ── 8. 로그아웃
    public void logout(Long memberUserId, String accessToken, String refreshToken){
        long remainingMs = jwtProvider.getRemainingExpiryMs(accessToken);
        if (remainingMs > 0) {
            redisService.addToBlacklist(accessToken, remainingMs);
        }
        redisService.deleteRefreshToken(memberUserId);
    }
    // ── 9. 비밀번호 재설정 요청
    public void sendPasswordReset(PasswordResetSendRequest request){// 존재 안하는 이메일도 성공 응답
        Optional<Member> memberOpt = memberRepository.findByEmail(request.getEmail());
        if (memberOpt.isPresent()){//값이 있으면
            String token = UUID.randomUUID().toString(); // 랜덤 토큰 사용
            redisService.savePasswordResetToken(token, request.getEmail(),1800); //30분 제한시간
            //emailService.sendPasswordResetEmail(request.getEmail(), token);찐으로 메일 보내는 코드
        }
    }

    // ── 10. 비밀번호 재설정 실행
    public void resetPassword(PasswordResetRequest request){
        String email = redisService.getEmailByPasswordResetToken(request.getToken());
        if (email == null){
            throw new BusinessException(ErrorCode.INVALID_PASSWORD_RESET_TOKEN);
        }

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        if (passwordEncoder.matches(request.getNewPassword(), member.getPassword())) {
            throw new BusinessException(ErrorCode.SAME_PASSWORD); // 기존이랑 같으면 에러 펑
        }

        member.changePassword(passwordEncoder.encode(request.getNewPassword()));
        redisService.deletePasswordResetToken(request.getToken());
        redisService.deleteRefreshToken(member.getMemberUserId());// 싹 다 무효화
    }

    // ── 11/12. 닉네임/이메일 중복 확인
    @Transactional(readOnly = true)
    public AvailableResponse checkNickname(String nickname){
        return AvailableResponse.of(!memberRepository.existsByNickname(nickname));
    }

    @Transactional(readOnly = true)
    public AvailableResponse checkEmail(String email){
        return AvailableResponse.of(!memberRepository.existsByEmail(email));
    }

    //기타
    private String generateSixDigitCode(){
        return String.format("%06d", (int) (Math.random() * 1_000_000));//무작위 6자리 수 만듦
    }

    public record TokenPair(SignupResponse response, String refreshToken){}
    public record LoginTokenPair(LoginResponse response, String refreshToken) {}
    public record GoogleLoginTokenPair(GoogleLoginResponse response, String refreshToken) {}
    public record RefreshTokenPair(TokenRefreshResponse response, String newRefreshToken) {}
}

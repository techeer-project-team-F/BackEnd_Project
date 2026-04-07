package com.shelfeed.backend.global.jwt;
//요청에서 JWT를 검증하고 SecurityContext에 사용자 정보 저장
import com.shelfeed.backend.global.redis.RedisService;
import com.shelfeed.backend.global.security.CustomUserDetailsService;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final CustomUserDetailsService userDetailsService;
    private final RedisService redisService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException{
        String token = extractToken(request);

        if (StringUtils.hasText(token)){
            try{
                if (jwtProvider.validateToken(token) && !redisService.isBlacklisted(token)){// 인증된 토큰 and No 블랙
                    Long memberUserId = jwtProvider.getMemberUserId(token); // 회원 번호 ID 꺼내기
                    UserDetails userDetails = userDetailsService.loadUserByUsername(String.valueOf(memberUserId)); //회원 찾기

                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userDetails, null , userDetails.getAuthorities());
                    authentication.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (ExpiredJwtException e){

            }
        }
        filterChain.doFilter(request,response);
    }

    private String extractToken(HttpServletRequest request){
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        } return null;
    }

}

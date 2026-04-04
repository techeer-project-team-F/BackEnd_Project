package com.shelfeed.backend.global.security;

import com.shelfeed.backend.domain.member.entity.Member;
import com.shelfeed.backend.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String memberUserId) throws UsernameNotFoundException {
        Member member = memberRepository.findByMemberUserId(Long.parseLong(memberUserId))
                .orElseThrow(() -> new UsernameNotFoundException(
                        "회원을 찾을 수 없습니다: " + memberUserId));
        return new CustomUserDetails(member);
    }

}

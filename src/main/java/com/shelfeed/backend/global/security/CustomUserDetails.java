package com.shelfeed.backend.global.security;

import com.shelfeed.backend.domain.member.entity.Member;
import com.shelfeed.backend.domain.member.enums.MemberStatus;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
public class CustomUserDetails implements UserDetails {

    private final Member member;

    public CustomUserDetails(Member member){
        this.member = member;
    }

    @Override
    public String getUsername() {
        return String.valueOf(member.getMemberUserId());
    }

    @Override
    public String getPassword(){
        return member.getPassword();

    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {//UserDetails인터페이스에 있는 메서드
        return List.of(new SimpleGrantedAuthority("ROLE_" + member.getRole().name()));
    }

    @Override
    public boolean isAccountNonLocked(){
        return member.getStatus() != MemberStatus.SUSPENDED;
    }

    @Override
    public  boolean isEnabled(){
        return member.getStatus() != MemberStatus.WITHDRAWN;
    }

}

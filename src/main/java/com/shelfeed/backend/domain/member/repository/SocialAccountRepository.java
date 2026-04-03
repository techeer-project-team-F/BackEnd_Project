package com.shelfeed.backend.domain.member.repository;

import com.shelfeed.backend.domain.member.entity.Member;
import com.shelfeed.backend.domain.member.entity.SocialAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SocialAccountRepository extends JpaRepository<SocialAccount, Long> {
    Optional<SocialAccount> findByProviderAndProviderId(String provider, String providerId);
}

package com.shelfeed.backend.domain.member.repository;

import com.shelfeed.backend.domain.member.entity.Member;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmail(String email);
    Optional<Member> findByMemberUserId(Long MemberUserId);
    boolean existsByEmail(String email);
    boolean existsByNickname(String nickname);

    //닉네임 검색
    @Query("""
    SELECT m FROM Member m WHERE m.nickname Like %:query%
    AND (:cursor IS NULL OR m.memberUserId < :cursor)
    ORDER BY m.memberUserId DESC
""")
    List<Member> searchMembers(@Param("query") String query,
                               @Param("cursor") Long cursor,
                               Pageable pageable);
}

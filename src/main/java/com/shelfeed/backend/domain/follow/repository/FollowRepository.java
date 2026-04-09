package com.shelfeed.backend.domain.follow.repository;

import com.shelfeed.backend.domain.follow.entity.Follow;
import com.shelfeed.backend.domain.member.entity.Member;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FollowRepository extends JpaRepository<Follow,Long> {

    boolean existsByFollowerAndFollowee(Member follower, Member followee);// 중복 팔로우 확인

    Optional<Follow> findByFollowerAndFollowee(Member follower, Member followee);// 삭제 대상 조회

    //팔로워 목록 페이지네이션
    @Query("""
            SELECT f FROM Follow f
            WHERE f.followee = :member
            AND (:cursor IS NULL OR f.followId< :cursor) ORDER BY f.followId DESC
""")
    List<Follow> findFollowers(@Param("member") Member member, @Param("cursor") Long cursor,
                               Pageable pageable);

    //팔로잉 목록 페이지네이션
    @Query("""
            SELECT f FROM Follow f
            WHERE f.follower = :member
            AND (:cursor IS NULL OR f.followId< :cursor) ORDER BY f.followId DESC
""")
    List<Follow> findFollowings(@Param("member") Member member, @Param("cursor") Long cursor,
                               Pageable pageable);

    //타 유저의 팔로워 조회
    List<Follow> findByFollowee(Member followee);
}

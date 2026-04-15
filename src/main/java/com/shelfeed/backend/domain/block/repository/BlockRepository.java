package com.shelfeed.backend.domain.block.repository;


import com.shelfeed.backend.domain.block.entity.Block;
import com.shelfeed.backend.domain.member.entity.Member;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.awt.print.Pageable;
import java.util.List;
import java.util.Optional;

public interface BlockRepository extends JpaRepository {
    //차단한 사람 입장에서 차단인을 차단했는가
    boolean existsByBlockerAndBlocked(Member blocker, Member blocked);
    //차단한 사람 입장에서 차단인 사람 찾기(유저랑 차단인 간의 관계)
    Optional<Block> findByBlockerAndBlocked(Member blocker, Member blocked);

    //차단 목록 페이지네이션
    @Query("""
            SELECT b FROM Block b
            WHERE b.blocker = :member
            AND (:cursor IS NULL OR b.blockId < :cursor)
            ORDER BY b.blockId DESC
    """)
    List<Block> findBlocks(@Param("member") Member member,
                           @Param("cursor") Long cursor,
                           Pageable pageable);
}

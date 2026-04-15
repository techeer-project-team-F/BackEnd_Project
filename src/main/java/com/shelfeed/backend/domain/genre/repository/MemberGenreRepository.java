package com.shelfeed.backend.domain.genre.repository;

import com.shelfeed.backend.domain.genre.entity.MemberGenre;
import com.shelfeed.backend.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;


//재 온보딩 시 유저의 선호장르 수정

public interface MemberGenreRepository extends JpaRepository<MemberGenre, Long> {
    void deleteAllByMember(Member member);
}


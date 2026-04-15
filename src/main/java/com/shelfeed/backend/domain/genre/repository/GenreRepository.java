package com.shelfeed.backend.domain.genre.repository;

import com.shelfeed.backend.domain.genre.entity.Genre;
import org.springframework.data.jpa.repository.JpaRepository;


//사용자의 프로필과 선호 장르를 영구 저장
public interface GenreRepository extends JpaRepository<Genre, Long> {
}

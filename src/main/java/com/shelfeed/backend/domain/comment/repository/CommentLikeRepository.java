package com.shelfeed.backend.domain.comment.repository;

import com.shelfeed.backend.domain.comment.entity.CommentLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CommentLikeRepository extends JpaRepository<CommentLike,Long> {

    boolean existsByCommentIdAndMemberId(Long commentId, Long memberId);//중복 조회

    Optional<CommentLike> findByCommentIdAndMemberId(Long commentId, Long memberId); //삭제 대상 조회 용도

}

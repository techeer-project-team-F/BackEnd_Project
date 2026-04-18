package com.shelfeed.backend.domain.comment.entity;

import com.shelfeed.backend.domain.member.entity.Member;
import com.shelfeed.backend.domain.review.entity.Review;
import com.shelfeed.backend.global.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "comments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment extends BaseTimeEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Long commentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    private Review review;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_comment_id")
    private Comment parentComment; //자기 참조

    @Column(nullable = false, length = 1000)
    private String content;

    @Column(nullable = false)
    private int likeCount = 0;

    @Column(nullable = false)
    private boolean isDeleted = false;

    private LocalDateTime deletedAt;

    // 1. 정적 메서드 첫댓글 작성할 때
    public static Comment createOriginComment(Review review, Member member, String content) {
        Comment comment = new Comment();
        comment.review = review;
        comment.member = member;
        comment.content = content;
        return comment;
    }

    // 2. 정적 메서드 대댓글을 작성할 때
    public static Comment createReply(Review review, Member member, Comment parentComment, String content) {
        if (parentComment == null) {
            throw new IllegalArgumentException("대댓글은 반드시 부모 댓글이 필요합니다!");
        }
        Comment comment = new Comment();
        comment.review = review;
        comment.member = member;
        comment.parentComment = parentComment;
        comment.content = content;
        return comment;
    }

    //비즈니스 메서드
    public void softDelete(){
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
    }

    public void update(String content){
        this.content = content;
    }

}

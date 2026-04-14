package com.shelfeed.backend.domain.review.entity;

import com.shelfeed.backend.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "review_likes", uniqueConstraints = @UniqueConstraint(columnNames = {"member_id","review_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class ReviewLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_like_id")
    private Long reviewLikeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    private Review review;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    //정적 메서드
    public static ReviewLike reviewLike(Member member, Review review) {
        ReviewLike reviewLike = new ReviewLike();
        reviewLike.member = member;
        reviewLike.review = review;
        return reviewLike;
    }

    public static ReviewLike create(Review review, Member member) {
        ReviewLike like = new ReviewLike();
        like.review = review;
        like.member = member;
        return like;
    }

}

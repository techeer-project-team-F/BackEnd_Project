package com.shelfeed.backend.domain.notification.entity;

import com.shelfeed.backend.domain.member.entity.Member;
import com.shelfeed.backend.domain.notification.enums.NotificationType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long notificationId;

    // 알림 받는 사람
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member receiver;

    // 알림 유발 행위자 (시스템 알림은 null)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_member_id")
    private Member actor;

    // 알림 종류에 따라 하나만 값이 채워짐 — JPA 관계 대신 ID만 저장 (유연성)
    @Column(name = "review_id")
    private Long reviewId;

    @Column(name = "review_like_id")
    private Long reviewLikeId;

    @Column(name = "comment_id")
    private Long commentId;

    @Column(name = "comment_like_id")
    private Long commentLikeId;

    @Column(name = "follow_id")
    private Long followId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NotificationType type;

    @Column(length = 500)
    private String message;

    @Column(nullable = false)
    private boolean isRead = false;

    @Column(nullable = false)
    private boolean isDeleted = false;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}

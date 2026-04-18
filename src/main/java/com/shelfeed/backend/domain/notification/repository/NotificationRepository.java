package com.shelfeed.backend.domain.notification.repository;

import com.shelfeed.backend.domain.member.entity.Member;
import com.shelfeed.backend.domain.notification.entity.Notification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Query("""
            SELECT n FROM Notification n
            LEFT JOIN FETCH n.actor
            WHERE n.receiver = :receiver
              AND n.isDeleted = false
              AND (:cursor IS NULL OR n.notificationId < :cursor)
            ORDER BY n.notificationId DESC
            """)
    List<Notification> findMyNotifications(@Param("receiver") Member receiver,
                                           @Param("cursor") Long cursor,
                                           Pageable pageable);
}


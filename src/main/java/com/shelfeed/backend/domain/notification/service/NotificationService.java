package com.shelfeed.backend.domain.notification.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shelfeed.backend.domain.member.entity.Member;
import com.shelfeed.backend.domain.member.repository.MemberRepository;
import com.shelfeed.backend.domain.notification.dto.response.NotificationItemResponse;
import com.shelfeed.backend.domain.notification.dto.response.NotificationListResponse;
import com.shelfeed.backend.domain.notification.dto.response.UnreadCountResponse;
import com.shelfeed.backend.domain.notification.entity.Notification;
import com.shelfeed.backend.domain.notification.repository.NotificationRepository;
import com.shelfeed.backend.global.common.exception.BusinessException;
import com.shelfeed.backend.global.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final MemberRepository memberRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public NotificationListResponse getMyNotifications(Long memberUserId, String cursor, int limit) {
         if (limit <= 0) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
        Member receiver = memberRepository.findByMemberUserId(memberUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        Long cursorId = decodeCursor(cursor);
        List<Notification> notifications = notificationRepository.findMyNotifications(
                receiver,
                cursorId,
                PageRequest.of(0, limit + 1)
        );

        List<NotificationItemResponse> content = notifications.stream()
                .map(NotificationItemResponse::of)
                .toList();

        String nextCursor = null;
        if (content.size() > limit) {
            Long nextId = content.get(limit - 1).getNotificationId();
            nextCursor = encodeCursor(nextId);
        }

        return NotificationListResponse.of(content, limit, nextCursor);
    }

    @Transactional
    public void markAsRead(Long memberUserId, Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOTIFICATION_NOT_FOUND));

        if (!notification.getReceiver().getMemberUserId().equals(memberUserId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        notification.beReaded();
    }

    public UnreadCountResponse getUnreadCount(Long memberUserId) {
        Member receiver = memberRepository.findByMemberUserId(memberUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        long unreadCount = notificationRepository.countUnread(receiver);
        return UnreadCountResponse.of(unreadCount);
    }

    private Long decodeCursor(String cursor) {
        if (cursor == null || cursor.isBlank()) return null;

        try {
            byte[] decoded = Base64.getDecoder().decode(cursor);
            String json = new String(decoded, StandardCharsets.UTF_8);
            CursorPayload payload = objectMapper.readValue(json, CursorPayload.class);
            return payload.id();
        } catch (IllegalArgumentException | JsonProcessingException e) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    private String encodeCursor(Long id) {
        try {
            String json = objectMapper.writeValueAsString(new CursorPayload(id));
            return Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
        } catch (JsonProcessingException e) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    private record CursorPayload(Long id) {}
}


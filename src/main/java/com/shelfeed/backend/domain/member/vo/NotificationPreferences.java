package com.shelfeed.backend.domain.member.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPreferences {
    @Builder.Default private boolean likeEnabled            = true;
    @Builder.Default private boolean commentEnabled         = true;
    @Builder.Default private boolean followEnabled          = true;
    @Builder.Default private boolean followingReviewEnabled = true;
}

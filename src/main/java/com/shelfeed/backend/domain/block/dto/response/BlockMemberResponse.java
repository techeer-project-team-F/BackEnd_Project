package com.shelfeed.backend.domain.block.dto.response;

import com.shelfeed.backend.domain.block.entity.Block;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class BlockMemberResponse {
    private Long userId;
    private String nickname;
    private String profileImageUrl;
    private LocalDateTime blockedAt;

    public static BlockMemberResponse of(Block block) {
        return BlockMemberResponse.builder()
                .userId(block.getBlocked().getMemberUserId())
                .nickname(block.getBlocked().getNickname())
                .profileImageUrl(block.getBlocked().getProfileImageUrl())
                .blockedAt(block.getCreatedAt())
                .build();
    }
}

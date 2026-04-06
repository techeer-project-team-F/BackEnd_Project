package com.shelfeed.backend.domain.member.dto.response;

import com.shelfeed.backend.domain.member.entity.Member;
import com.shelfeed.backend.domain.member.enums.LibraryVisibility;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UpdateProfileResponse {
    private Long userId;
    private String nickname;
    private String profileImageUrl;
    private String bio;
    private LibraryVisibility libraryVisibility;

    public static UpdateProfileResponse of(Member member){
        return UpdateProfileResponse.builder()
                .userId(member.getMemberUserId())
                .nickname(member.getNickname())
                .profileImageUrl(member.getProfileImageUrl())
                .bio(member.getBio())
                .libraryVisibility(member.getLibraryVisibility())
                .build();
    }

}

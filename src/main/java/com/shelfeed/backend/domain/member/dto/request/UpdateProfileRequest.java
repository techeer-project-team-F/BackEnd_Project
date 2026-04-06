package com.shelfeed.backend.domain.member.dto.request;

import com.shelfeed.backend.domain.member.enums.LibraryVisibility;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class UpdateProfileRequest {
    @Size(max = 50, message = "닉네임은 50자 이내로 입력해주세요.")
    private String nickname;

    private String profileImageUrl;

    @Size(max = 300, message = "소개는 300자 이내로 입력해주세요.")
    private String bio;

    private LibraryVisibility libraryVisibility;
}

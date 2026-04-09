package com.shelfeed.backend.domain.library.dto.request;

import com.shelfeed.backend.domain.library.enums.ReadingStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class LibraryBookAddRequest {

    @NotNull(message = "도서 ID는 필수입니다.")
    private Long bookId;

    @NotNull(message = "독서 상태는 필수입니다.")
    private ReadingStatus status;

}

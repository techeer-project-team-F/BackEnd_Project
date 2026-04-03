package com.shelfeed.backend.domain.auth.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AvailableResponse {
    private boolean available;

    public static AvailableResponse of(boolean available){
        return AvailableResponse.builder()
                .available(available)
                .build();
    }

}

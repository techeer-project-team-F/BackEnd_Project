package com.shelfeed.backend.domain.book.dto.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class AladinBookItem {
    private String title;
    private String author;
    private String publisher;
    private String pubDate;
    private String isbn13;
    private long itemId;
    private String cover;
    private String description;
    private SubInfo subInfo;

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SubInfo {
        private Integer itemPage;
    }
}

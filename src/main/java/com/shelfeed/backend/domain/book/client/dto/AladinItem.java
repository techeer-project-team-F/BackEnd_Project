package com.shelfeed.backend.domain.book.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class AladinItem {
    private String isbn13;
    private String title;
    private String author;
    private String publisher;
    private String cover;
    private String description;
    private String pubDate;
    private Long itemId;
    private String categoryName;

    @JsonProperty("subInfo")
    private SubInfo subInfo;

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SubInfo{
        private Integer itemPage;
    }

}

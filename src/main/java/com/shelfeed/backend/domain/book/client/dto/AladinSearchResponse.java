package com.shelfeed.backend.domain.book.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)//역직렬화(JSON->JAVA 객체)
public class AladinSearchResponse {
    @JsonProperty("item")
    private List<AladinItem> items;
}

package com.shelfeed.backend.domain.book.dto.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;

import java.util.List;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class AladinSearchResponse {
    private int totalResults;
    private int startIndex;
    private int itemsPerPage;
    private List<AladinBookItem> item;
}

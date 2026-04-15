package com.shelfeed.backend.domain.search.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SearchResponse {

    public SearchPageResponse<BookSearchResult> books;
    public SearchPageResponse<UserSearchResult> users;
}

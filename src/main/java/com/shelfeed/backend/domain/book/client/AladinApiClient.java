package com.shelfeed.backend.domain.book.client;

import com.shelfeed.backend.domain.book.dto.external.AladinSearchResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@RequiredArgsConstructor
public class AladinApiClient {

    private static final String ALADIN_SEARCH_URL = "http://www.aladin.co.kr/ttb/api/ItemSearch.aspx";

    private final RestTemplate restTemplate;

    @Value("${aladin.api.ttbkey}")
    private String ttbKey;

    @Value("${aladin.api.version}")
    private String version;

    public AladinSearchResponse searchBooks(String query, int page, int maxResults) {
        String url = UriComponentsBuilder.fromHttpUrl(ALADIN_SEARCH_URL)
                .queryParam("TTBKey", ttbKey)
                .queryParam("Query", query)
                .queryParam("QueryType", "Keyword")
                .queryParam("MaxResults", maxResults)
                .queryParam("start", page)
                .queryParam("SearchTarget", "Book")
                .queryParam("output", "js")
                .queryParam("Version", version)
                .build()
                .toUriString();

        return restTemplate.getForObject(url, AladinSearchResponse.class);
    }
}

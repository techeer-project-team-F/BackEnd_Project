package com.shelfeed.backend.domain.search.repository;

import com.shelfeed.backend.domain.search.entity.SearchHistory;
import org.springframework.data.jpa.repository.JpaRepository;
//기본적인 것만
public interface SearchHistoryRepository extends JpaRepository<SearchHistory, Long> {
}

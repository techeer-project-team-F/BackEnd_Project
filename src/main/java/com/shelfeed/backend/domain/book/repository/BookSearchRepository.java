package com.shelfeed.backend.domain.book.repository;

import com.shelfeed.backend.domain.book.document.BookDocument;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface BookSearchRepository extends ElasticsearchRepository<BookDocument, Long> {

    @Query("""
            {
              "bool": {
                "should": [
                  { "match": { "title":  { "query": "?0", "boost": 2.0 } } },
                  { "match": { "author": { "query": "?0", "boost": 1.0 } } }
                ],
                "minimum_should_match": 1
              }
            }
            """)
    List<BookDocument> searchByTitleOrAuthor(String query, Pageable pageable);
}

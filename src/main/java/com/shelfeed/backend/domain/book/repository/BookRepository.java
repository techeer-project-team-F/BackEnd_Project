package com.shelfeed.backend.domain.book.repository;

import com.shelfeed.backend.domain.book.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long> {

    Optional<Book> findByIsbn13(String isbn13); //isbn바코드 조회 확인
    //도서의 평균 평점 쿼리
    @Query("""
SELECT AVG(r.rating) FROM Review r WHERE r.book.bookId = :bookId AND r.isDeleted = false
            AND r.reviewVisibility = 'PUBLIC' AND r.reviewStatus = 'PUBLISHED'""")
    Double findAverageRatingByBookId(@Param("bookId") Long bookId);

    // 도서의 감상 수 쿼리
    @Query("""
            SELECT COUNT(r) FROM Review r WHERE r.book.bookId = :bookId AND r.isDeleted = false
            AND r.reviewVisibility = 'PUBLIC' AND r.reviewStatus = 'PUBLISHED'
""")
    Long countReviewsByBookId(@Param("bookId") Long bookId);
}

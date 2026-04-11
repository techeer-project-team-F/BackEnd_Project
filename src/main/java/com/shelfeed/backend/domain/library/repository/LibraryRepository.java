package com.shelfeed.backend.domain.library.repository;

import com.shelfeed.backend.domain.library.entity.LibraryBook;
import com.shelfeed.backend.domain.library.enums.ReadingStatus;
import com.shelfeed.backend.domain.member.entity.Member;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface LibraryRepository extends JpaRepository<LibraryBook,Long> {
    //중복 확인
    boolean existsByMemberIdAndBook_BookId(Member member, Long bookId);
    //내 서재 목록 JPQL
    @Query("SELECT lb FROM LibraryBook lb WHERE lb.memberId = :member " +
            "AND (:status IS NULL OR lb.status = :status) " +//전체 조회 + 필터링 조회
            "AND (:cursor IS NULL OR lb.libraryBookId < :cursor) " + // 커서 페이지 네이션
            "ORDER BY lb.libraryBookId DESC")
    List<LibraryBook> findMyLibrary(@Param("member") Member member, @Param("status") ReadingStatus status,
                                    @Param("cursor") Long cursor,
                                    Pageable pageable);

    //유저 본인의 서재인가 확인
    Optional<LibraryBook> findByLibraryBookIdAndMemberId(Long libraryBookId, Member member);

    // 도서 상세 조회 시 사용자의 해당 도서 서재 상태 조회
    Optional<LibraryBook> findByMemberIdAndBook_BookId(Member member, Long bookId);

    // 도서 검색 결과 중 서재에 담긴 isbn13 배치 조회
    @Query("SELECT lb.book.isbn13 FROM LibraryBook lb WHERE lb.memberId = :member AND lb.book.isbn13 IN :isbn13List")
    Set<String> findIsbn13InLibrary(@Param("member") Member member, @Param("isbn13List") List<String> isbn13List);

    //타 유저 서재 목록
    @Query("SELECT lb FROM LibraryBook lb WHERE lb.memberId = :member " +
            "AND (:status IS NULL OR lb.status = :status) " +//전체 조회 + 필터링 조회
            "AND (:cursor IS NULL OR lb.libraryBookId < :cursor) " +// 커서 페이지 네이션
            "ORDER BY lb.libraryBookId DESC")
    List<LibraryBook> findUserLibrary(@Param("member") Member member, @Param("status") ReadingStatus status,
                                      @Param("cursor") Long cursor,
                                      Pageable pageable);

}


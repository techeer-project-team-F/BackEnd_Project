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

    //타 유저 서재 목록
    @Query("SELECT lb FROM LibraryBook lb WHERE lb.memberId = :member " +
            "AND (:status IS NULL OR lb.status = :status) " +//전체 조회 + 필터링 조회
            "AND (:cursor IS NULL OR lb.libraryBookId < :cursor) " +// 커서 페이지 네이션
            "ORDER BY lb.libraryBookId DESC")
    List<LibraryBook> findUserLibrary(@Param("member") Member member, @Param("status") ReadingStatus status,
                                      @Param("cursor") Long cursor,
                                      Pageable pageable);
    Optional<LibraryBook> findByMemberIdAndBook_BookId(Member member,Long bookId);

    // 서재에 담긴 도서 ID 목록 일괄 조회 (N+1 방지)
    @Query("SELECT lb.book.bookId FROM LibraryBook lb WHERE lb.memberId = :member AND lb.book.bookId IN :bookIds")
    Set<Long> findBookIdsByMemberAndBookIdIn(@Param("member") Member member, @Param("bookIds") List<Long> bookIds);

}


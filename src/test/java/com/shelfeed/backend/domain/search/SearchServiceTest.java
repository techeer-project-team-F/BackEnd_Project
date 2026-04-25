package com.shelfeed.backend.domain.search;

import com.shelfeed.backend.domain.book.entity.Book;
import com.shelfeed.backend.domain.book.repository.BookRepository;
import com.shelfeed.backend.domain.follow.repository.FollowRepository;
import com.shelfeed.backend.domain.member.entity.Member;
import com.shelfeed.backend.domain.member.repository.MemberRepository;
import com.shelfeed.backend.domain.search.dto.response.SearchResponse;
import com.shelfeed.backend.domain.search.entity.SearchHistory;
import com.shelfeed.backend.domain.search.repository.SearchHistoryRepository;
import com.shelfeed.backend.domain.search.service.SearchService;
import com.shelfeed.backend.global.common.exception.BusinessException;
import com.shelfeed.backend.global.common.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class SearchServiceTest {

    @Mock MemberRepository memberRepository;
    @Mock BookRepository bookRepository;
    @Mock FollowRepository followRepository;
    @Mock SearchHistoryRepository searchHistoryRepository;

    @InjectMocks SearchService searchService;

    Member member;
    Member targetUser;
    Book book;

    @BeforeEach
    void setUp() {
        member = Member.createLocal(1L, "me@test.com", "encoded", "me", "bio");
        targetUser = Member.createLocal(2L, "target@test.com", "encoded", "target", "bio");

        book = mock(Book.class);
        lenient().when(book.getBookId()).thenReturn(10L);
        lenient().when(book.getIsbn13()).thenReturn("9781234567890");
        lenient().when(book.getTitle()).thenReturn("Test Book");
        lenient().when(book.getAuthor()).thenReturn("Author");
        lenient().when(book.getCoverImageUrl()).thenReturn("http://cover.url");
    }

    @Nested
    @DisplayName("통합 검색")
    class Search {

        @Test
        @DisplayName("성공 - type=all, 로그인 상태 (검색 기록 저장, 팔로잉 여부 포함)")
        void 성공_all_로그인() {
            Object[] stat = new Object[]{10L, 4.5, 5L};
            given(memberRepository.findByMemberUserId(1L)).willReturn(Optional.of(member));
            given(bookRepository.searchBooks(eq("test"), isNull(), any(PageRequest.class))).willReturn(List.of(book));
            given(bookRepository.findReviewStatsByBooks(List.of(book))).willReturn(Collections.singletonList(stat));
            given(memberRepository.searchMembers(eq("test"), isNull(), any(PageRequest.class))).willReturn(List.of(targetUser));
            given(followRepository.findFollowingIds(eq(member), anyList())).willReturn(Set.of(2L));

            SearchResponse response = searchService.search("test", "all", null, 10, 1L);

            assertThat(response.getBooks().getContent()).hasSize(1);
            assertThat(response.getUsers().getContent()).hasSize(1);
            then(searchHistoryRepository).should().save(any(SearchHistory.class));
        }

        @Test
        @DisplayName("성공 - type=book (유저 검색 생략)")
        void 성공_type_book() {
            Object[] stat = new Object[]{10L, 3.0, 2L};
            given(memberRepository.findByMemberUserId(1L)).willReturn(Optional.of(member));
            given(bookRepository.searchBooks(eq("test"), isNull(), any(PageRequest.class))).willReturn(List.of(book));
            given(bookRepository.findReviewStatsByBooks(List.of(book))).willReturn(Collections.singletonList(stat));

            SearchResponse response = searchService.search("test", "book", null, 10, 1L);

            assertThat(response.getBooks().getContent()).hasSize(1);
            assertThat(response.getUsers().getContent()).isEmpty();
            then(memberRepository).should(never()).searchMembers(any(), any(), any());
        }

        @Test
        @DisplayName("성공 - type=user (도서 검색 생략)")
        void 성공_type_user() {
            given(memberRepository.findByMemberUserId(1L)).willReturn(Optional.of(member));
            given(memberRepository.searchMembers(eq("test"), isNull(), any(PageRequest.class))).willReturn(List.of(targetUser));
            given(followRepository.findFollowingIds(eq(member), anyList())).willReturn(Set.of());

            SearchResponse response = searchService.search("test", "user", null, 10, 1L);

            assertThat(response.getUsers().getContent()).hasSize(1);
            assertThat(response.getBooks().getContent()).isEmpty();
            then(bookRepository).should(never()).searchBooks(any(), any(), any());
        }

        @Test
        @DisplayName("성공 - 비회원 (검색 기록 미저장, 팔로잉 여부 미조회)")
        void 성공_비회원() {
            given(bookRepository.searchBooks(eq("test"), isNull(), any(PageRequest.class))).willReturn(List.of());
            given(memberRepository.searchMembers(eq("test"), isNull(), any(PageRequest.class))).willReturn(List.of());

            SearchResponse response = searchService.search("test", "all", null, 10, null);

            assertThat(response).isNotNull();
            then(searchHistoryRepository).should(never()).save(any());
            then(followRepository).should(never()).findFollowingIds(any(), any());
        }

        @Test
        @DisplayName("성공 - 검색 결과 없을 때 리뷰 통계 쿼리 생략")
        void 성공_검색결과_없음_통계_생략() {
            given(memberRepository.findByMemberUserId(1L)).willReturn(Optional.of(member));
            given(bookRepository.searchBooks(eq("없는책"), isNull(), any(PageRequest.class))).willReturn(List.of());
            given(memberRepository.searchMembers(eq("없는책"), isNull(), any(PageRequest.class))).willReturn(List.of());

            SearchResponse response = searchService.search("없는책", "all", null, 10, 1L);

            assertThat(response.getBooks().getContent()).isEmpty();
            then(bookRepository).should(never()).findReviewStatsByBooks(any());
        }

        @Test
        @DisplayName("INVALID_SEARCH_TYPE 예외 - 유효하지 않은 type 값")
        void type_잘못된_값_예외() {
            assertThatThrownBy(() -> searchService.search("test", "invalid_type", null, 10, null))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_SEARCH_TYPE);
        }

        @Test
        @DisplayName("INVALID_SEARCH_TYPE 예외 - null type 값")
        void type_null_예외() {
            assertThatThrownBy(() -> searchService.search("test", null, null, 10, null))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_SEARCH_TYPE);
        }

        @Test
        @DisplayName("SEARCH_QUERY_REQUIRED 예외 - null 쿼리")
        void 쿼리_null_예외() {
            assertThatThrownBy(() -> searchService.search(null, "all", null, 10, 1L))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SEARCH_QUERY_REQUIRED);
        }

        @Test
        @DisplayName("SEARCH_QUERY_REQUIRED 예외 - 공백 쿼리")
        void 쿼리_blank_예외() {
            assertThatThrownBy(() -> searchService.search("   ", "all", null, 10, 1L))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SEARCH_QUERY_REQUIRED);
        }
    }
}

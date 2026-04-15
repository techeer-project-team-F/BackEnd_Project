package com.shelfeed.backend.domain.search.service;

import com.shelfeed.backend.domain.book.entity.Book;
import com.shelfeed.backend.domain.book.repository.BookRepository;
import com.shelfeed.backend.domain.follow.repository.FollowRepository;
import com.shelfeed.backend.domain.member.entity.Member;
import com.shelfeed.backend.domain.member.repository.MemberRepository;
import com.shelfeed.backend.domain.search.dto.response.BookSearchResult;
import com.shelfeed.backend.domain.search.dto.response.SearchPageResponse;
import com.shelfeed.backend.domain.search.dto.response.SearchResponse;
import com.shelfeed.backend.domain.search.dto.response.UserSearchResult;
import com.shelfeed.backend.domain.search.entity.SearchHistory;
import com.shelfeed.backend.domain.search.repository.SearchHistoryRepository;
import com.shelfeed.backend.global.common.exception.BusinessException;
import com.shelfeed.backend.global.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SearchService {

    private final MemberRepository memberRepository;
    private final BookRepository bookRepository;
    private final FollowRepository followRepository;
    private final SearchHistoryRepository searchHistoryRepository;

    //통합 검색
    @Transactional
    public SearchResponse search (String query, String type, Long cursor, int limit, Long memberUserId){
        //쿼리 여부 확인
        if (query == null || query.isBlank()) {
            throw new BusinessException(ErrorCode.SEARCH_QUERY_REQUIRED);
        }
        //로그인 시 검색 기록 저장
        if (memberUserId != null) {
            Member member = getMember(memberUserId);
            searchHistoryRepository.save(SearchHistory.create(member, query.trim()));
        }

        SearchPageResponse<BookSearchResult> books = SearchPageResponse.empty();
        SearchPageResponse<UserSearchResult> users = SearchPageResponse.empty();

        if (type.equals("all") || type.equals("book")) { books = searchBooks(query, cursor, limit); }
        if (type.equals("all") || type.equals("user")) { users = searchUsers(query, cursor, limit, memberUserId); }

        return SearchResponse.builder()
                .books(books)
                .users(users)
                .build();
    }

    // 도서 검색
    private SearchPageResponse<BookSearchResult> searchBooks(String query, Long cursor, int limit) {
        List<Book> books = bookRepository.searchBooks(query, cursor, PageRequest.of(0, limit + 1));
        //페이지네이션
        boolean hasNext = books.size() > limit;
        List<Book> result = hasNext ? books.subList(0, limit) : books;
        List<BookSearchResult> content = result.stream().map(book ->
                BookSearchResult.of(book, bookRepository.findAverageRatingByBookId(book.getBookId()),
                        bookRepository.countReviewsByBookId(book.getBookId()))
        ).toList();

        Long nextCursor = hasNext ? result.get(result.size() - 1).getBookId() : null;

        return SearchPageResponse.<BookSearchResult>builder()
                .content(content)
                .nextCursor(nextCursor)
                .hasNext(hasNext)
                .size(content.size())
                .build();
    }

    // 유저 검색
    private SearchPageResponse<UserSearchResult> searchUsers(String query, Long cursor,
                                                             int limit, Long memberUserId) {
        List<Member> members = memberRepository.searchMembers(query, cursor, PageRequest.of(0, limit + 1));
        //페이지네이션
        boolean hasNext = members.size() > limit;
        List<Member> result = hasNext ? members.subList(0, limit) : members;
        //내가 팔로잉을 했는가
        List<UserSearchResult> content = result.stream().map(target -> {
            boolean isFollowing = false;
            if (memberUserId != null) {
                Member me = getMember(memberUserId);
                isFollowing = followRepository.existsByFollowerAndFollowee(me, target);
            }
            return UserSearchResult.of(target, isFollowing);
        }).toList();

        Long nextCursor = hasNext ? result.get(result.size() - 1).getMemberUserId() : null;

        return SearchPageResponse.<UserSearchResult>builder()
                .content(content)
                .nextCursor(nextCursor)
                .hasNext(hasNext)
                .size(content.size())
                .build();
    }

    private Member getMember(Long memberUserId) {
        return memberRepository.findByMemberUserId(memberUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
    }
}

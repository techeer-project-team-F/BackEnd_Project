package com.shelfeed.backend.domain.feed.dto.response;

import com.shelfeed.backend.domain.feed.entity.Feed;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class FeedItemResponse {

    private Long feedId;
    private ReviewInfo review;

    @Getter
    @Builder
    public static class ReviewInfo {
        private Long reviewId;
        private UserInfo user;
        private BookInfo book;
        private int rating;
        private String content;
        private String quote;
        private Boolean isSpoiler;
        private int likeCount;
        private int commentCount;
        private Boolean isLiked;
        private List<String> tags;
        private LocalDateTime createdAt;
    }
    @Getter
    @Builder
    public static class UserInfo {
        private Long userId;
        private String nickname;
        private String profileImageUrl;
    }

    @Getter
    @Builder
    public static class BookInfo {
        private Long bookId;
        private String title;
        private String author;
        private String coverImageUrl;
    }

    public static FeedItemResponse of(Feed feed, boolean isLiked, List<String> tags) {
        return FeedItemResponse.builder()
                .feedId(feed.getFeedId())
                .review(ReviewInfo.builder()
                        .reviewId(feed.getReview().getReviewId())
                        .user(UserInfo.builder()
                                .userId(feed.getReview().getMember().getMemberUserId())
                                .nickname(feed.getReview().getMember().getNickname())
                                .profileImageUrl(feed.getReview().getMember().getProfileImageUrl())
                                .build())
                        .book(BookInfo.builder()
                                .bookId(feed.getReview().getBook().getBookId())
                                .title(feed.getReview().getBook().getTitle())
                                .author(feed.getReview().getBook().getAuthor())
                                .coverImageUrl(feed.getReview().getBook().getCoverImageUrl())
                                .build())
                        .rating(feed.getReview().getRating())
                        .content(feed.getReview().getContent())
                        .quote(feed.getReview().getQuote())
                        .isSpoiler(feed.getReview().isSpoiler())
                        .likeCount(feed.getReview().getLikeCount())
                        .commentCount(feed.getReview().getCommentCount())
                        .isLiked(isLiked)
                        .tags(tags)
                        .createdAt(feed.getReview().getCreatedAt())
                        .build())
                .build();
    }
}

package com.shelfeed.backend.domain.member.dto.response;

import com.shelfeed.backend.domain.genre.entity.Genre;
import com.shelfeed.backend.domain.member.entity.Member;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class OnboardingResponse {

    private Long memberId;
    private String nickname;
    private String profileImageUrl;
    private String bio;
    private boolean onboardingCompleted;
    private List<GenreInfo> genres;

    @Getter
    @Builder
    public static class GenreInfo {
        private Long genreId;
        private String name;

        public static GenreInfo of(Genre genre) {
            return GenreInfo.builder()
                    .genreId(genre.getGenreId())
                    .name(genre.getGenreName())
                    .build();
        }
    }

    public static OnboardingResponse of(Member member, List<Genre> genres) {
        return OnboardingResponse.builder()
                .memberId(member.getMemberId())
                .nickname(member.getNickname())
                .profileImageUrl(member.getProfileImageUrl())
                .bio(member.getBio())
                .onboardingCompleted(member.isOnboardingCompleted())
                .genres(genres.stream().map(GenreInfo::of).toList())
                .build();
    }
}

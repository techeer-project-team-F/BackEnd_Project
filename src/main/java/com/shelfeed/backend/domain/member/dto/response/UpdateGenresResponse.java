package com.shelfeed.backend.domain.member.dto.response;

import com.shelfeed.backend.domain.genre.entity.Genre;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class UpdateGenresResponse {
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

    public static UpdateGenresResponse of(List<Genre> genres) {
        return UpdateGenresResponse.builder()
                .genres(genres.stream().map(GenreInfo::of).toList())
                .build();
    }
}


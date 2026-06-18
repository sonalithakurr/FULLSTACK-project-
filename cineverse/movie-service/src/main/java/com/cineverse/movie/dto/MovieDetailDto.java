package com.cineverse.movie.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Full movie detail DTO — includes cast, trailers, etc.
 */
@Data
public class MovieDetailDto {
    private String id;
    private String title;
    private String description;
    private LocalDate releaseDate;
    private String contentRating;
    private Integer runtimeMinutes;
    private BigDecimal averageRating;
    private Integer reviewCount;
    private String posterUrl;
    private String backdropUrl;
    private String trailerUrl;
    private List<String> genres;
    private List<PersonDto> cast;
    private List<PersonDto> directors;

    @Data
    public static class PersonDto {
        private String id;
        private String name;
        private String role;
        private String photoUrl;
    }
}

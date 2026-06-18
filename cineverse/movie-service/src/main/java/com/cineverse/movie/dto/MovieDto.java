package com.cineverse.movie.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Movie DTO — returned in paginated lists (lightweight, no cast details)
 */
@Data
public class MovieDto {
    private String id;
    private String title;
    private String description;
    private LocalDate releaseDate;
    private String contentRating;
    private Integer runtimeMinutes;
    private BigDecimal averageRating;
    private Integer reviewCount;
    private String posterUrl;
    private List<String> genres;
}

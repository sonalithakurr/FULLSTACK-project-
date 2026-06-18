package com.cineverse.review.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

@Data
public class CreateReviewRequest {

    @NotBlank
    private String movieId;

    @Min(1) @Max(10)
    private int rating;

    @NotBlank
    @Size(max = 150, message = "Review title must be under 150 characters")
    private String title;

    @NotBlank
    @Size(min = 10, max = 5000, message = "Review must be 10–5000 characters")
    private String content;

    private List<String> tags;

    private boolean containsSpoilers;
}

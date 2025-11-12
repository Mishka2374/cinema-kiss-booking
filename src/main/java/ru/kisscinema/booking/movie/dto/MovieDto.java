package ru.kisscinema.booking.movie.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record MovieDto(
        Long id,
        @NotBlank String title,
        @Min(1) Integer durationMinutes,
        String description
) {}
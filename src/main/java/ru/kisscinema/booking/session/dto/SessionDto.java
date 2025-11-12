package ru.kisscinema.booking.session.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SessionDto(
        Long id,
        @NotNull Long movieId,
        @NotNull Long hallId,
        @NotNull @Future LocalDateTime startTime,
        @NotNull @DecimalMin(value = "0.01") BigDecimal price
) {}
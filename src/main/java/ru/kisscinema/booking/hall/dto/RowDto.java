package ru.kisscinema.booking.hall.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record RowDto(
        @NotNull Long hallId,
        @Min(1) Integer rowNumber
) {}

package ru.kisscinema.booking.hall.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AddSeatsDto(@Min(1) @NotNull Integer count) {}
package ru.kisscinema.booking.booking.dto;

import jakarta.validation.constraints.NotNull;

public record BookingRequestDto(
        @NotNull Long sessionId,
        @NotNull Long seatId
) {}
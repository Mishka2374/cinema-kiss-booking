package ru.kisscinema.booking.booking.dto;

import jakarta.validation.constraints.NotNull;

public record BookingDto(
        @NotNull Long sessionId,
        @NotNull Long seatId,
        Long chatId
) {}
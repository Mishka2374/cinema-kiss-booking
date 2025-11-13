package ru.kisscinema.booking.booking.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Входящий запрос от пользователя (через Postman или Telegram-бот).
 * Содержит только то, что нужно для создания брони
 */
public record BookingRequestDto(
        @NotNull Long sessionId,
        @NotNull Long seatId
) {}
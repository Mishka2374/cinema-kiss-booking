package ru.kisscinema.booking.booking.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Ответ пользователю — всё, что ему нужно знать после успешной брони.
 */
public record BookingResponse(
        String bookingCode,
        String movieTitle,
        LocalDateTime sessionTime,
        BigDecimal price,
        Integer rowNumber,
        Integer seatNumber
) {}
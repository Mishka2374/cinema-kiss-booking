package ru.kisscinema.booking.booking.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BookingResponse(
        String bookingCode,
        String movieTitle,
        LocalDateTime sessionTime,
        BigDecimal price,
        Integer rowNumber,
        Integer seatNumber
) {}
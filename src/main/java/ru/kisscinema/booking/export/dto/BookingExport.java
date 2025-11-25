package ru.kisscinema.booking.export.dto;

import java.time.LocalDateTime;

public record BookingExport(
        String bookingCode,
        String movieTitle,
        String hallName,
        LocalDateTime sessionTime,
        Integer rowNumber,
        Integer seatNumber,
        String status,
        Long telegramUserId
) {}
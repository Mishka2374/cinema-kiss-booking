package ru.kisscinema.booking.hall.dto;

public record SeatDto(
        Long id,
        Integer rowNumber,
        Integer seatNumber
) {}
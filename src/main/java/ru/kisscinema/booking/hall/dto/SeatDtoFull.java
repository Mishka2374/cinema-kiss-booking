package ru.kisscinema.booking.hall.dto;

public record SeatDtoFull(
        Long id,
        Integer rowNumber,
        Integer seatNumber,
        boolean taken,
        boolean mine,
        boolean used
) {}

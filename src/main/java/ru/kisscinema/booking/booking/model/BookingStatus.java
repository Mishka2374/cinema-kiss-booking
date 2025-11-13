package ru.kisscinema.booking.booking.model;

public enum BookingStatus {
    RESERVED,   // активная бронь
    USED,       // подтверждена на кассе
    CANCELLED   // отменена пользователем
}
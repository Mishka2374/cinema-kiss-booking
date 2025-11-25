package ru.kisscinema.booking.export.dto;

import java.util.List;

public record ExportData(
        List<HallExport> halls,
        List<MovieExport> movies,
        List<SessionExport> sessions,
        List<BookingExport> bookings
) {}
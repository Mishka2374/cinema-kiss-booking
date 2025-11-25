package ru.kisscinema.booking.export.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SessionExport(
        String movieTitle,
        String hallName,
        LocalDateTime startTime,
        BigDecimal price
) {}
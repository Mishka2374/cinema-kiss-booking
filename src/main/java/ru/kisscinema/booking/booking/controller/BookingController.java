package ru.kisscinema.booking.booking.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import ru.kisscinema.booking.booking.dto.*;
import ru.kisscinema.booking.booking.service.BookingService;
import ru.kisscinema.booking.hall.dto.SeatDtoFull;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private static final Logger log = LoggerFactory.getLogger(BookingController.class);
    private final BookingService bookingService;

    /**
     * POST /api/bookings
     * Забронировать место — возвращает код, фильм, время, цену, ряд и место.
     */
    @PostMapping
    public BookingResponse createBooking(
            @Valid @RequestBody BookingRequestDto dto,
            @RequestParam(required = false) Long telegramUserId) {

        log.info("Бронирование места: сеанс ID {}, место ID {}, пользователь {}",
                dto.sessionId(), dto.seatId(), telegramUserId);

        BookingResponse response = bookingService.createBooking(dto, telegramUserId);

        log.info("Место успешно забронировано. Код: {}, фильм: {}, ряд: {}, место: {}",
                response.bookingCode(), response.movieTitle(), response.rowNumber(), response.seatNumber());

        return response;
    }

    /**
     * GET /api/bookings/sessions/{sessionId}/seats
     * Получить список мест для сеанса с полной информацией (занятость, принадлежность пользователю).
     */
    @GetMapping("/sessions/{sessionId}/seats")
    public List<SeatDtoFull> getSeatsFull(
            @PathVariable Long sessionId,
            @RequestParam(required = false) Long telegramUserId) {

        log.info("Получение мест для сеанса ID: {} пользователя {}", sessionId, telegramUserId);

        List<SeatDtoFull> seats = bookingService.getSeatsFull(sessionId, telegramUserId);

        log.info("Места для сеанса ID {} получены. Всего мест: {}", sessionId, seats.size());

        return seats;
    }

    /**
     * POST /api/bookings/use
     * Подтвердить использование брони на кассе по уникальному коду (параметр bookingCode).
     */
    @PostMapping("/use")
    public void useBooking(@RequestParam String bookingCode) {
        log.info("Подтверждение использования брони с кодом: {}", bookingCode);
        bookingService.useBooking(bookingCode);
        log.info("Бронь с кодом {} успешно подтверждена на кассе", bookingCode);
    }

    /**
     * POST /api/bookings/{id}/cancel
     * Отменить бронь (только если статус RESERVED).
     */
    @PostMapping("/{id}/cancel")
    public void cancelBooking(@PathVariable Long id) {
        log.info("Отмена брони с ID: {}", id);
        bookingService.cancelBooking(id);
        log.info("Бронь с ID {} успешно отменена", id);
    }
}

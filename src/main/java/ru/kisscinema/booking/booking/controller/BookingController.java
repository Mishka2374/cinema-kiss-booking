package ru.kisscinema.booking.booking.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import ru.kisscinema.booking.booking.model.Booking;
import ru.kisscinema.booking.booking.dto.BookingDto;
import ru.kisscinema.booking.booking.service.BookingService;

import java.util.List;

/**
 * REST контроллер для бронирования и продажи билетов.
 */
@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private static final Logger log = LoggerFactory.getLogger(BookingController.class);
    private final BookingService bookingService;

    /**
     * Получить список всех броней.
     */
    @GetMapping
    public List<Booking> getAll(@RequestParam(required = false) Long chatId) {
        if (chatId != null) {
            log.info("Получение броней для пользователя (chatId: {})...", chatId);
        } else {
            log.info("Получение списка всех броней...");
        }
        List<Booking> bookings = bookingService.getAllBookings(chatId);
        log.info("Список броней успешно получен. Количество: {}", bookings.size());
        return bookings;
    }

    /**
     * Получить информацию о брони по ID.
     */
    @GetMapping("/{id}")
    public Booking get(@PathVariable Long id) {
        log.info("Получение брони с ID: {}", id);
        Booking booking = bookingService.getBookingById(id);
        log.info("Бронь с ID {} успешно получена", id);
        return booking;
    }

    /**
     * Создать новую бронь (место резервируется как RESERVED).
     */
    @PostMapping
    public Booking create(@Valid @RequestBody BookingDto dto) {
        log.info("Создание брони: сеанс ID {}, место ID {}, пользователь chatId {}",
                dto.sessionId(), dto.seatId(), dto.chatId());
        Booking saved = bookingService.createBooking(dto);
        log.info("Бронь успешно создана с ID {}", saved.getId());
        return saved;
    }

    /**
     * Подтвердить оплату брони (изменить статус на PAID).
     */
    @PostMapping("/{id}/pay")
    public Booking pay(@PathVariable Long id) {
        log.info("Подтверждение оплаты брони с ID: {}", id);
        Booking paid = bookingService.payBooking(id);
        log.info("Бронь с ID {} успешно оплачена", id);
        return paid;
    }

    /**
     * Получить список ID свободных мест для сеанса.
     */
    @GetMapping("/sessions/{sessionId}/available-seats")
    public List<Long> getAvailableSeats(@PathVariable Long sessionId) {
        log.info("Получение списка свободных мест для сеанса с ID: {}", sessionId);
        List<Long> seats = bookingService.getAvailableSeats(sessionId);
        log.info("Свободные места для сеанса ID {} получены. Количество: {}", sessionId, seats.size());
        return seats;
    }
}
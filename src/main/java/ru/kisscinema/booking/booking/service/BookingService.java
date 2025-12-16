package ru.kisscinema.booking.booking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kisscinema.booking.audit.service.AuditService;
import ru.kisscinema.booking.audit.util.AuditAuthor;
import ru.kisscinema.booking.booking.dto.BookingRequestDto;
import ru.kisscinema.booking.booking.dto.BookingResponse;
import ru.kisscinema.booking.booking.model.Booking;
import ru.kisscinema.booking.booking.model.BookingStatus;
import ru.kisscinema.booking.booking.repository.BookingRepository;
import ru.kisscinema.booking.hall.dto.SeatDtoFull;
import ru.kisscinema.booking.session.repository.SessionRepository;
import ru.kisscinema.booking.hall.repository.SeatRepository;
import ru.kisscinema.booking.session.model.Session;
import ru.kisscinema.booking.hall.model.Seat;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final SessionRepository sessionRepository;
    private final SeatRepository seatRepository;
    private final AuditService auditService;

    @Transactional
    public BookingResponse createBooking(BookingRequestDto dto, Long telegramUserId) {
        Session session = sessionRepository.findById(dto.sessionId())
                .orElseThrow(() -> new RuntimeException("Сеанс не найден"));

        Seat seat = seatRepository.findById(dto.seatId())
                .orElseThrow(() -> new RuntimeException("Место не найдено"));

        Optional<Booking> existing = bookingRepository
                .findBySessionIdAndSeatId(dto.sessionId(), dto.seatId());

        // вычисляем totalRows через seatRepository, т.к. у Hall нет getRows()
        Long hallId = session.getHall().getId();
        List<Seat> hallSeats = seatRepository.findByRowHallId(hallId);
        int totalRows = (int) hallSeats.stream()
                .map(s -> s.getRow().getRowNumber())
                .distinct()
                .count();

        // Защита: если почему-то нет рядов — считаем 1
        if (totalRows <= 0) totalRows = 1;

        int rowNumber = seat.getRow().getRowNumber();
        BigDecimal finalPrice = session.getPrice().multiply(getRowCoefficient(rowNumber, totalRows));

        // Если бронь уже существует
        if (existing.isPresent()) {
            Booking b = existing.get();

            if (b.getStatus() == BookingStatus.RESERVED) {
                throw new RuntimeException("Место уже забронировано");
            }

            if (b.getStatus() == BookingStatus.USED) {
                throw new RuntimeException("Билет уже использован, место нельзя забронировать");
            }

            if (b.getStatus() == BookingStatus.CANCELLED) {

                b.setStatus(BookingStatus.RESERVED);
                b.setUserTelegramId(telegramUserId); // может быть null
                b.setBookingCode("BK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());

                // Если у тебя есть поле price в Booking — можно сохранить, но т.к. setPrice отсутствует, пропускаем.
                bookingRepository.save(b);

                auditService.log(
                        "Booking",
                        b.getId(),
                        "UPDATE",
                        telegramUserId == null ? "SYSTEM" : AuditAuthor.USER,
                        "Бронь восстановлена. Код: " + b.getBookingCode()
                                + ", сеанс: " + session.getId()
                                + ", место: " + rowNumber + "-" + seat.getSeatNumber()
                                + (telegramUserId == null ? "" : ", пользователь: " + telegramUserId)
                );

                return new BookingResponse(
                        b.getBookingCode(),
                        session.getMovie().getTitle(),
                        session.getStartTime(),
                        finalPrice,
                        rowNumber,
                        seat.getSeatNumber()
                );
            }
        }

        // Создаём новую бронь
        Booking booking = new Booking();
        booking.setSession(session);
        booking.setSeat(seat);
        booking.setUserTelegramId(telegramUserId); // может быть null
        booking.setBookingCode("BK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        booking.setStatus(BookingStatus.RESERVED);

        bookingRepository.save(booking);

        auditService.log(
                "Booking",
                booking.getId(),
                "CREATE",
                telegramUserId == null ? "SYSTEM" : AuditAuthor.USER,
                "Создана бронь. Код: " + booking.getBookingCode()
                        + ", сеанс: " + session.getId()
                        + ", место: " + rowNumber + "-" + seat.getSeatNumber()
                        + (telegramUserId == null ? "" : ", пользователь: " + telegramUserId)
        );

        return new BookingResponse(
                booking.getBookingCode(),
                session.getMovie().getTitle(),
                session.getStartTime(),
                finalPrice,
                rowNumber,
                seat.getSeatNumber()
        );
    }

    @Transactional(readOnly = true)
    public List<SeatDtoFull> getSeatsFull(Long sessionId, Long userChatId) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Сеанс не найден"));

        Long hallId = session.getHall().getId();

        // Все места зала
        List<Seat> allSeats = seatRepository.findByRowHallId(hallId);

        int rowsCount = (int) allSeats.stream()
                .map(s -> s.getRow().getRowNumber())
                .distinct()
                .count();

        int totalRows = rowsCount <= 0 ? 1 : rowsCount; // НЕ изменяем rowsCount — он final

        return allSeats.stream().map(seat -> {

                    Optional<Booking> bookingOpt =
                            bookingRepository.findBySessionIdAndSeatId(sessionId, seat.getId());

                    boolean taken = false;
                    boolean mine = false;
                    boolean used = false;

                    if (bookingOpt.isPresent()) {
                        Booking b = bookingOpt.get();
                        taken = (b.getStatus() == BookingStatus.RESERVED || b.getStatus() == BookingStatus.USED);
                        mine = (b.getUserTelegramId() != null && b.getUserTelegramId().equals(userChatId));
                        used = (b.getStatus() == BookingStatus.USED);
                    }

            int row = seat.getRow().getRowNumber();
            BigDecimal finalPrice = session.getPrice().multiply(getRowCoefficient(row, totalRows));

            return new SeatDtoFull(
                    seat.getId(),
                    row,
                    seat.getSeatNumber(),
                    taken,
                    mine,
                    used,
                    finalPrice
            );

        }).toList();
    }

    @Transactional
    public BigDecimal getRowCoefficient(int row, int totalRows) {
        if (totalRows <= 1) {
            // маленькие залы, цена не корректируем
            return BigDecimal.valueOf(1.0);
        }

        // Настройки: какие ряды считаются передними и задними
        int frontLimit = 1;               // передний 1 ряд

        if (row <= frontLimit) {
            return BigDecimal.valueOf(0.5); // передние — немного дешевле
        }

        if (row >= totalRows) { // последние 1 ряд
            return BigDecimal.valueOf(1.00); // задние — базовая цена
        }

        // средние ряды — лучшие, увеличиваем цену
        return BigDecimal.valueOf(2.00);
    }


    @Transactional
    public void useBooking(String bookingCode) {
        Booking booking = bookingRepository.findByBookingCode(bookingCode)
                .orElseThrow(() -> new RuntimeException("Бронь не найдена"));
        booking.setStatus(BookingStatus.USED);
        bookingRepository.save(booking);

        auditService.log("Booking", booking.getId(), "UPDATE", AuditAuthor.USER,
                "Бронь подтверждена на кассе. Код: " + bookingCode);
    }

    @Transactional
    public Long getSeatId(Long sessionId, int rowNumber, int seatNumber) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Сеанс не найден"));

        Long hallId = session.getHall().getId();

        return seatRepository
                .findByRowHallIdAndRowRowNumberAndSeatNumber(hallId, rowNumber, seatNumber)
                .map(Seat::getId)
                .orElseThrow(() -> new RuntimeException(
                        "Место " + rowNumber + "-" + seatNumber + " не найдено в зале"));
    }


    @Transactional
    public void cancelBooking(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Бронь не найдена"));

        if (booking.getStatus() != BookingStatus.RESERVED) {
            throw new RuntimeException("Нельзя отменить бронь: статус не RESERVED");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        auditService.log("Booking", id, "UPDATE", AuditAuthor.USER, "Бронь отменена");
    }

    @Transactional
    public void cancelBookingByUser(Long sessionId, int rowNumber, int seatNumber, Long chatId) {
        // Получаем ID места по ряду и номеру места
        Long seatId = getSeatId(sessionId, rowNumber, seatNumber);

        // Находим бронь, привязанную к этому сеансу, месту и пользователю
        Booking booking = bookingRepository.findBySessionIdAndSeatIdAndUserTelegramId(sessionId, seatId, chatId)
                .orElseThrow(() -> new RuntimeException("Бронь не найдена для этого пользователя"));

        if (booking.getStatus() != BookingStatus.RESERVED) {
            throw new RuntimeException("Нельзя отменить бронь: статус не RESERVED");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        auditService.log("Booking", booking.getId(), "UPDATE", AuditAuthor.USER, "Бронь отменена пользователем");
    }
}
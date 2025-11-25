package ru.kisscinema.booking.export.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kisscinema.booking.export.dto.*;
import ru.kisscinema.booking.hall.model.Hall;
import ru.kisscinema.booking.hall.model.Row;
import ru.kisscinema.booking.hall.model.Seat;
import ru.kisscinema.booking.movie.model.Movie;
import ru.kisscinema.booking.session.model.Session;
import ru.kisscinema.booking.booking.model.Booking;
import ru.kisscinema.booking.booking.model.BookingStatus;
import ru.kisscinema.booking.hall.repository.HallRepository;
import ru.kisscinema.booking.hall.repository.RowRepository;
import ru.kisscinema.booking.hall.repository.SeatRepository;
import ru.kisscinema.booking.movie.repository.MovieRepository;
import ru.kisscinema.booking.session.repository.SessionRepository;
import ru.kisscinema.booking.booking.repository.BookingRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExportService {

    private final HallRepository hallRepository;
    private final MovieRepository movieRepository;
    private final SessionRepository sessionRepository;
    private final RowRepository rowRepository;
    private final SeatRepository seatRepository;
    private final BookingRepository bookingRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    private final Path EXPORT_DIR = Paths.get("exports");

    // === ЭКСПОРТ ===

    @Transactional(readOnly = true)
    public void exportToFile() throws IOException {
        Files.createDirectories(EXPORT_DIR);

        List<HallExport> halls = hallRepository.findAll().stream()
                .map(h -> new HallExport(h.getName(), h.getDescription()))
                .toList();

        List<MovieExport> movies = movieRepository.findAll().stream()
                .map(m -> new MovieExport(m.getTitle(), m.getDurationMinutes(), m.getDescription()))
                .toList();

        List<SessionExport> sessions = sessionRepository.findAll().stream()
                .map(s -> new SessionExport(
                        s.getMovie().getTitle(),
                        s.getHall().getName(),
                        s.getStartTime(),
                        s.getPrice()
                ))
                .toList();

        List<BookingExport> bookings = bookingRepository.findAll().stream()
                .map(b -> new BookingExport(
                        b.getBookingCode(),
                        b.getSession().getMovie().getTitle(),
                        b.getSession().getHall().getName(),
                        b.getSession().getStartTime(),
                        b.getSeat().getRow().getRowNumber(),
                        b.getSeat().getSeatNumber(),
                        b.getStatus().name(),
                        b.getUserTelegramId()
                ))
                .toList();

        ExportData exportData = new ExportData(halls, movies, sessions, bookings);

        String filename = "cinema_export_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")) +
                ".json";
        Path path = EXPORT_DIR.resolve(filename);
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(path.toFile(), exportData);
    }

    // === ИМПОРТ ===

    @Transactional
    public void importFromFile(String filename) throws IOException {
        if (filename == null || filename.isBlank()) {
            throw new IllegalArgumentException("Имя файла не может быть пустым");
        }
        Path path = EXPORT_DIR.resolve(filename).normalize();
        if (!path.startsWith(EXPORT_DIR.normalize()) || !Files.exists(path)) {
            throw new IllegalArgumentException("Файл не найден: " + filename);
        }

        ExportData data = objectMapper.readValue(path.toFile(), ExportData.class);

        // Очистка
        bookingRepository.deleteAll();
        sessionRepository.deleteAll();
        seatRepository.deleteAll();
        rowRepository.deleteAll();
        movieRepository.deleteAll();
        hallRepository.deleteAll();
        entityManager.clear();

        // Залы
        Map<String, Hall> hallMap = new HashMap<>();
        for (HallExport dto : data.halls()) {
            Hall hall = new Hall();
            hall.setName(dto.name());
            hall.setDescription(dto.description());
            hallMap.put(dto.name(), hallRepository.save(hall));
        }

        // Фильмы
        Map<String, Movie> movieMap = new HashMap<>();
        for (MovieExport dto : data.movies()) {
            Movie movie = new Movie();
            movie.setTitle(dto.title());
            movie.setDurationMinutes(dto.durationMinutes());
            movie.setDescription(dto.description());
            movieMap.put(dto.title(), movieRepository.save(movie));
        }

        // Ряды и места — пересоздаём по залам
        Map<String, Map<Integer, Map<Integer, Seat>>> seatLookup = new HashMap<>();
        for (Hall hall : hallMap.values()) {
            // Определяем, какие ряды и места использовались в бронях
            Set<Integer> usedRowNumbers = data.bookings().stream()
                    .filter(b -> b.hallName().equals(hall.getName()))
                    .map(BookingExport::rowNumber)
                    .collect(Collectors.toSet());

            Map<Integer, Map<Integer, Seat>> rowSeatMap = new HashMap<>();
            for (Integer rowNumber : usedRowNumbers) {
                // Создаём ряд
                Row row = new Row();
                row.setHall(hall);
                row.setRowNumber(rowNumber);
                Row savedRow = rowRepository.save(row);

                // Определяем, какие места в ряду использовались
                Set<Integer> usedSeatNumbers = data.bookings().stream()
                        .filter(b -> b.hallName().equals(hall.getName()) && b.rowNumber().equals(rowNumber))
                        .map(BookingExport::seatNumber)
                        .collect(Collectors.toSet());

                Map<Integer, Seat> seatMap = new HashMap<>();
                for (Integer seatNumber : usedSeatNumbers) {
                    Seat seat = new Seat();
                    seat.setRow(savedRow);
                    seat.setSeatNumber(seatNumber);
                    Seat savedSeat = seatRepository.save(seat);
                    seatMap.put(seatNumber, savedSeat);
                }
                rowSeatMap.put(rowNumber, seatMap);
            }
            seatLookup.put(hall.getName(), rowSeatMap);
        }

        // Сеансы
        List<Session> sessions = new ArrayList<>();
        for (SessionExport dto : data.sessions()) {
            Session s = new Session();
            s.setMovie(movieMap.get(dto.movieTitle()));
            s.setHall(hallMap.get(dto.hallName()));
            s.setStartTime(dto.startTime());
            s.setEndTime(dto.startTime().plusMinutes(
                    movieMap.get(dto.movieTitle()).getDurationMinutes()
            ));
            s.setPrice(dto.price());
            sessions.add(s);
        }
        List<Session> savedSessions = sessionRepository.saveAll(sessions);

        // Брони — связываем по совпадению: фильм + зал + время + ряд + место
        for (BookingExport dto : data.bookings()) {
            // Находим сеанс
            Session targetSession = savedSessions.stream()
                    .filter(s -> s.getMovie().getTitle().equals(dto.movieTitle())
                            && s.getHall().getName().equals(dto.hallName())
                            && s.getStartTime().equals(dto.sessionTime()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Сеанс не найден для брони: " + dto.bookingCode()));

            // Находим место
            Seat targetSeat = seatLookup
                    .get(dto.hallName())
                    .get(dto.rowNumber())
                    .get(dto.seatNumber());

            Booking booking = new Booking();
            booking.setSession(targetSession);
            booking.setSeat(targetSeat);
            booking.setBookingCode(dto.bookingCode());
            booking.setStatus(BookingStatus.valueOf(dto.status()));
            booking.setUserTelegramId(dto.telegramUserId());
            bookingRepository.save(booking);
        }
    }
}
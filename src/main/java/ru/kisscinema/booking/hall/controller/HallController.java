package ru.kisscinema.booking.hall.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import ru.kisscinema.booking.hall.dto.*;
import ru.kisscinema.booking.hall.model.Row;
import ru.kisscinema.booking.hall.model.Seat;
import ru.kisscinema.booking.hall.service.HallService;

import java.util.List;

/**
 * REST контроллер для управления кинозалами.
 */
@RestController
@RequestMapping("/api/halls")
@RequiredArgsConstructor
public class HallController {

    private static final Logger log = LoggerFactory.getLogger(HallController.class);
    private final HallService hallService;

    /**
     * Получить список всех кинозалов.
     */
    @GetMapping
    public List<HallDto> getAllHalls() {
        log.info("Получение списка всех кинозалов...");
        List<HallDto> halls = hallService.getAllHalls();
        log.info("Список кинозалов успешно получен. Количество: {}", halls.size());
        return halls;
    }

    /**
     * Получить информацию о конкретном кинозале по ID.
     */
    @GetMapping("/{id}")
    public HallDto getHall(@PathVariable Long id) {
        log.info("Получение информации о кинозале с ID: {}", id);
        HallDto hall = hallService.getHallById(id);
        log.info("Кинозал с ID {} успешно получен", id);
        return hall;
    }

    /**
     * Создать новый кинозал.
     */
    @PostMapping
    public HallDto createHall(@Valid @RequestBody HallDto dto) {
        log.info("Создание нового кинозала: {}", dto.name());
        HallDto saved = hallService.createHall(dto);
        log.info("Кинозал '{}' успешно создан с ID {}", saved.name(), saved.id());
        return saved;
    }

    /**
     * Получить все ряды в указанном кинозале.
     */
    @GetMapping("/{id}/rows")
    public List<Row> getRows(@PathVariable Long id) {
        log.info("Получение всех рядов в зале с ID: {}", id);
        List<Row> rows = hallService.getRowsByHallId(id);
        log.info("Ряды в зале ID {} успешно получены. Количество: {}", id, rows.size());
        return rows;
    }

    /**
     * Добавить новый ряд в кинозал.
     */
    @PostMapping("/{id}/rows")
    public Row addRow(@PathVariable Long id, @Valid @RequestBody RowDto dto) {
        log.info("Добавление ряда {} в зал с ID: {}", dto.rowNumber(), id);
        Row row = hallService.addRow(id, dto);
        log.info("Ряд с номером {} успешно добавлен в зал ID {}", dto.rowNumber(), id);
        return row;
    }

    /**
     * Получить все места в указанном ряду.
     */
    @GetMapping("/rows/{rowId}/seats")
    public List<Seat> getSeats(@PathVariable Long rowId) {
        log.info("Получение всех мест в ряду с ID: {}", rowId);
        List<Seat> seats = hallService.getSeatsByRowId(rowId);
        log.info("Места в ряду ID {} успешно получены. Количество: {}", rowId, seats.size());
        return seats;
    }

    /**
     * Добавить указанное количество мест в ряд (нумерация с 1).
     */
    @PostMapping("/rows/{rowId}/seats")
    public List<Seat> addSeats(@PathVariable Long rowId, @Valid @RequestBody AddSeatsDto dto) {
        log.info("Добавление {} мест в ряд с ID: {}", dto.count(), rowId);
        List<Seat> seats = hallService.addSeats(rowId, dto);
        log.info("Места успешно добавлены в ряд ID {}. Количество: {}", rowId, seats.size());
        return seats;
    }
}
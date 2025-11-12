package ru.kisscinema.booking.hall.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
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
     * GET /api/halls
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
     * GET /api/halls/{id}
     * Получить информацию о кинозале по ID.
     */
    @GetMapping("/{id}")
    public HallDto getHall(@PathVariable Long id) {
        log.info("Получение кинозала с ID: {}", id);
        HallDto hall = hallService.getHallById(id);
        log.info("Кинозал с ID {} успешно получен", id);
        return hall;
    }

    /**
     * POST /api/halls
     * Создать новый кинозал.
     */
    @PostMapping
    public HallDto createHall(@Valid @RequestBody HallDto dto) {
        log.info("Создание кинозала: {}", dto.name());
        HallDto saved = hallService.createHall(dto);
        log.info("Кинозал '{}' успешно создан с ID {}", saved.name(), saved.id());
        return saved;
    }

    /**
     * GET /api/halls/{id}/rows
     * Получить все ряды в указанном зале.
     */
    @GetMapping("/{id}/rows")
    public List<Row> getRows(@PathVariable Long id) {
        log.info("Получение рядов в зале ID: {}", id);
        List<Row> rows = hallService.getRowsByHallId(id);
        log.info("Ряды в зале ID {} получены. Количество: {}", id, rows.size());
        return rows;
    }

    /**
     * POST /api/halls/{id}/rows
     * Добавить новый ряд в зал.
     */
    @PostMapping("/{id}/rows")
    public Row addRow(@PathVariable Long id, @Valid @RequestBody RowDto dto) {
        log.info("Добавление ряда {} в зал ID: {}", dto.rowNumber(), id);
        Row row = hallService.addRow(id, dto);
        log.info("Ряд {} добавлен в зал ID {}", dto.rowNumber(), id);
        return row;
    }

    /**
     * GET /api/rows/{rowId}/seats
     * Получить все места в указанном ряду.
     */
    @GetMapping("/rows/{rowId}/seats")
    public List<Seat> getSeats(@PathVariable Long rowId) {
        log.info("Получение мест в ряду ID: {}", rowId);
        List<Seat> seats = hallService.getSeatsByRowId(rowId);
        log.info("Места в ряду ID {} получены. Количество: {}", rowId, seats.size());
        return seats;
    }

    /**
     * POST /api/rows/{rowId}/seats
     * Добавить указанное количество мест в ряд.
     */
    @PostMapping("/rows/{rowId}/seats")
    public List<Seat> addSeats(@PathVariable Long rowId, @Valid @RequestBody AddSeatsDto dto) {
        log.info("Добавление {} мест в ряд ID: {}", dto.count(), rowId);
        List<Seat> seats = hallService.addSeats(rowId, dto);
        log.info("Места добавлены в ряд ID {}. Количество: {}", rowId, seats.size());
        return seats;
    }

    /**
     * DELETE /api/halls/{id}
     * Удалить кинозал (только если нет сеансов).
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHall(@PathVariable Long id) {
        log.info("Удаление кинозала с ID: {}", id);
        hallService.deleteHall(id);
        log.info("Кинозал с ID {} успешно удалён", id);
        return ResponseEntity.noContent().build();
    }
}
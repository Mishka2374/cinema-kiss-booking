package ru.kisscinema.booking.hall.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kisscinema.booking.hall.dto.*;
import ru.kisscinema.booking.hall.model.Hall;
import ru.kisscinema.booking.hall.model.Row;
import ru.kisscinema.booking.hall.model.Seat;
import ru.kisscinema.booking.hall.repository.*;
import ru.kisscinema.booking.session.repository.SessionRepository;

import java.util.List;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class HallService {

    private final HallRepository hallRepository;
    private final RowRepository rowRepository;
    private final SeatRepository seatRepository;
    private final SessionRepository sessionRepository;

    @Transactional(readOnly = true)
    public List<HallDto> getAllHalls() {
        return hallRepository.findAll().stream()
                .map(h -> new HallDto(h.getId(), h.getName(), h.getDescription()))
                .toList();
    }

    @Transactional(readOnly = true)
    public HallDto getHallById(Long id) {
        Hall hall = hallRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Hall not found"));
        return new HallDto(hall.getId(), hall.getName(), hall.getDescription());
    }

    @Transactional
    public HallDto createHall(HallDto dto) {
        Hall hall = new Hall();
        hall.setName(dto.name());
        hall.setDescription(dto.description());
        Hall saved = hallRepository.save(hall);
        return new HallDto(saved.getId(), saved.getName(), saved.getDescription());
    }

    @Transactional(readOnly = true)
    public List<Row> getRowsByHallId(Long hallId) {
        return rowRepository.findByHallIdOrderByRowNumberAsc(hallId);
    }

    @Transactional
    public Row addRow(Long hallId, RowDto dto) {
        Hall hall = hallRepository.findById(hallId)
                .orElseThrow(() -> new RuntimeException("Hall not found"));
        Row row = new Row();
        row.setHall(hall);
        row.setRowNumber(dto.rowNumber());
        return rowRepository.save(row);
    }

    @Transactional(readOnly = true)
    public List<Seat> getSeatsByRowId(Long rowId) {
        return seatRepository.findByRowIdOrderBySeatNumberAsc(rowId);
    }

    @Transactional
    public List<Seat> addSeats(Long rowId, AddSeatsDto dto) {
        Row row = rowRepository.findById(rowId)
                .orElseThrow(() -> new RuntimeException("Row not found"));
        List<Seat> seats = IntStream.rangeClosed(1, dto.count())
                .mapToObj(num -> {
                    Seat seat = new Seat();
                    seat.setRow(row);
                    seat.setSeatNumber(num);
                    return seat;
                })
                .toList();
        return seatRepository.saveAll(seats);
    }

    @Transactional
    public void deleteHall(Long id) {
        if (sessionRepository.existsByHallId(id)) {
            throw new RuntimeException("Невозможно удалить зал: существуют сеансы в этом зале");
        }
        hallRepository.deleteById(id);
    }
}
package ru.kisscinema.booking.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.ResponseEntity;
import ru.kisscinema.booking.hall.controller.HallController;
import ru.kisscinema.booking.hall.dto.*;
import ru.kisscinema.booking.hall.model.Row;
import ru.kisscinema.booking.hall.model.Seat;
import ru.kisscinema.booking.hall.service.HallService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HallControllerTest {

    @InjectMocks
    private HallController hallController;

    @Mock
    private HallService hallService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getAllHalls_shouldReturnList() {
        HallDto hall1 = new HallDto(1L, "Hall 1", "Descripition is...");
        HallDto hall2 = new HallDto(2L, "Hall 2", "Descripition is...2");
        when(hallService.getAllHalls()).thenReturn(List.of(hall1, hall2));

        List<HallDto> result = hallController.getAllHalls();

        assertEquals(2, result.size());
        assertEquals("Hall 1", result.get(0).name());
        verify(hallService, times(1)).getAllHalls();
    }

    @Test
    void getHall_shouldReturnHall() {
        HallDto hall = new HallDto(1L, "Hall 1","Descripition is...");
        when(hallService.getHallById(1L)).thenReturn(hall);

        HallDto result = hallController.getHall(1L);

        assertEquals(1L, result.id());
        assertEquals("Hall 1", result.name());
        verify(hallService, times(1)).getHallById(1L);
    }

    @Test
    void createHall_shouldReturnCreatedHall() {
        HallDto dto = new HallDto(null, "New Hall", "Descripition is...");
        HallDto saved = new HallDto(1L, "New Hall", "Descripition is...");
        when(hallService.createHall(dto)).thenReturn(saved);

        HallDto result = hallController.createHall(dto);

        assertEquals(1L, result.id());
        assertEquals("New Hall", result.name());
        verify(hallService, times(1)).createHall(dto);
    }

    @Test
    void getRows_shouldReturnList() {
        Row row1 = new Row();
        Row row2 = new Row();
        when(hallService.getRowsByHallId(1L)).thenReturn(List.of(row1, row2));

        List<Row> result = hallController.getRows(1L);

        assertEquals(2, result.size());
        verify(hallService, times(1)).getRowsByHallId(1L);
    }

    @Test
    void addRow_shouldReturnRow() {
        RowDto dto = new RowDto((long) 1,  1);
        Row row = new Row();
        when(hallService.addRow(1L, dto)).thenReturn(row);

        Row result = hallController.addRow(1L, dto);

        assertNotNull(result);
        verify(hallService, times(1)).addRow(1L, dto);
    }

    @Test
    void getSeats_shouldReturnList() {
        Seat seat1 = new Seat();
        Seat seat2 = new Seat();
        when(hallService.getSeatsByRowId(1L)).thenReturn(List.of(seat1, seat2));

        List<Seat> result = hallController.getSeats(1L);

        assertEquals(2, result.size());
        verify(hallService, times(1)).getSeatsByRowId(1L);
    }

    @Test
    void addSeats_shouldReturnList() {
        AddSeatsDto dto = new AddSeatsDto(2);
        Seat seat1 = new Seat();
        Seat seat2 = new Seat();
        when(hallService.addSeats(1L, dto)).thenReturn(List.of(seat1, seat2));

        List<Seat> result = hallController.addSeats(1L, dto);

        assertEquals(2, result.size());
        verify(hallService, times(1)).addSeats(1L, dto);
    }

    @Test
    void deleteHall_shouldReturnNoContent() {
        ResponseEntity<Void> response = hallController.deleteHall(1L);

        assertEquals(204, response.getStatusCodeValue());
        verify(hallService, times(1)).deleteHall(1L);
    }

    @Test
    void deleteRow_shouldReturnNoContent() {
        ResponseEntity<Void> response = hallController.deleteRow(1L);

        assertEquals(204, response.getStatusCodeValue());
        verify(hallService, times(1)).deleteRow(1L);
    }
}

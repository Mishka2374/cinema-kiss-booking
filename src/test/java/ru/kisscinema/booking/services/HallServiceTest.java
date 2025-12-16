package ru.kisscinema.booking.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import ru.kisscinema.booking.audit.service.AuditService;
import ru.kisscinema.booking.hall.dto.*;
import ru.kisscinema.booking.hall.model.Hall;
import ru.kisscinema.booking.hall.model.Row;
import ru.kisscinema.booking.hall.model.Seat;
import ru.kisscinema.booking.hall.repository.*;
import ru.kisscinema.booking.hall.service.HallService;
import ru.kisscinema.booking.session.repository.SessionRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HallServiceTest {

    @InjectMocks
    private HallService hallService;

    @Mock
    private HallRepository hallRepository;

    @Mock
    private RowRepository rowRepository;

    @Mock
    private SeatRepository seatRepository;

    @Mock
    private SessionRepository sessionRepository;

    @Mock
    private AuditService auditService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getAllHalls_shouldReturnList() {
        Hall h1 = new Hall(); h1.setId(1L); h1.setName("Hall1");
        Hall h2 = new Hall(); h2.setId(2L); h2.setName("Hall2");
        when(hallRepository.findAll()).thenReturn(List.of(h1, h2));

        List<HallDto> result = hallService.getAllHalls();

        assertEquals(2, result.size());
        assertEquals("Hall1", result.get(0).name());
        verify(hallRepository, times(1)).findAll();
    }

    @Test
    void getHallById_shouldReturnHall() {
        Hall hall = new Hall(); hall.setId(1L); hall.setName("Hall1");
        when(hallRepository.findById(1L)).thenReturn(Optional.of(hall));

        HallDto result = hallService.getHallById(1L);

        assertEquals("Hall1", result.name());
        verify(hallRepository, times(1)).findById(1L);
    }

    @Test
    void createHall_shouldReturnCreatedHall() {
        HallDto dto = new HallDto(null, "New Hall", "Desc");
        Hall saved = new Hall(); saved.setId(1L); saved.setName("New Hall");
        when(hallRepository.save(any(Hall.class))).thenReturn(saved);

        HallDto result = hallService.createHall(dto);

        assertEquals(1L, result.id());
        assertEquals("New Hall", result.name());
        verify(auditService, times(1)).log(eq("Hall"), eq(1L), eq("CREATE"), any(), anyString());
    }

    @Test
    void addRow_shouldReturnRow() {
        Hall hall = new Hall(); hall.setId(1L);
        RowDto dto = new RowDto( (long)5, 1);
        Row savedRow = new Row(); savedRow.setId((long)10); savedRow.setRowNumber(5);
        when(hallRepository.findById(1L)).thenReturn(Optional.of(hall));
        when(rowRepository.save(any(Row.class))).thenReturn(savedRow);

        Row result = hallService.addRow(1L, dto);

        assertEquals(5, result.getRowNumber());
        verify(auditService, times(1)).log(eq("Row"), eq(10L), eq("CREATE"), any(), anyString());
    }

    @Test
    void addSeats_shouldReturnSeats() {
        Row row = new Row(); row.setId(1L);
        AddSeatsDto dto = new AddSeatsDto(3);
        List<Seat> savedSeats = List.of(new Seat(), new Seat(), new Seat());

        when(rowRepository.findById(1L)).thenReturn(Optional.of(row));
        when(seatRepository.saveAll(anyList())).thenReturn(savedSeats);

        List<Seat> result = hallService.addSeats(1L, dto);

        assertEquals(3, result.size());
        verify(auditService, times(1)).log(eq("Seat"), isNull(), eq("CREATE"), any(), anyString());
    }

    @Test
    void deleteHall_shouldCallRepository() {
        when(sessionRepository.existsByHallId(1L)).thenReturn(false);

        hallService.deleteHall(1L);

        verify(hallRepository, times(1)).deleteById(1L);
        verify(auditService, times(1)).log(eq("Hall"), eq(1L), eq("DELETE"), any(), anyString());
    }

    @Test
    void deleteRow_shouldCallRepository() {
        when(rowRepository.existsBookingsByRowId(1L)).thenReturn(false);

        hallService.deleteRow(1L);

        verify(rowRepository, times(1)).deleteById(1L);
        verify(auditService, times(1)).log(eq("Row"), eq(1L), eq("DELETE"), any(), anyString());
    }
}

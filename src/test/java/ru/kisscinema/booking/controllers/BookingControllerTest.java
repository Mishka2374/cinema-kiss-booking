package ru.kisscinema.booking.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import ru.kisscinema.booking.booking.dto.*;
import ru.kisscinema.booking.booking.service.BookingService;
import ru.kisscinema.booking.booking.controller.BookingController;
import ru.kisscinema.booking.hall.dto.SeatDtoFull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BookingControllerTest {

    @InjectMocks
    private BookingController bookingController;

    @Mock
    private BookingService bookingService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createBooking_shouldReturnBookingResponse() {
        // Arrange
        BookingRequestDto request = new BookingRequestDto(1L, 10L);
        BookingResponse response = new BookingResponse(
                "BK-12345678", "Movie Title", LocalDateTime.now(), new BigDecimal(500), 3, 10
        );
        when(bookingService.createBooking(request, 12345L)).thenReturn(response);

        // Act
        BookingResponse result = bookingController.createBooking(request, 12345L);

        // Assert
        assertNotNull(result);
        assertEquals("BK-12345678", result.bookingCode());
        verify(bookingService, times(1)).createBooking(request, 12345L);
    }

    @Test
    void getSeatsFull_shouldReturnListOfSeats() {
        // Arrange
        SeatDtoFull seat1 = new SeatDtoFull(1L, 1, 1, false, false, false, BigDecimal.ONE);
        SeatDtoFull seat2 = new SeatDtoFull(2L, 1, 2, true, true, false, BigDecimal.ONE);
        List<SeatDtoFull> seats = List.of(seat1, seat2);

        when(bookingService.getSeatsFull(1L, 12345L)).thenReturn(seats);

        // Act
        List<SeatDtoFull> result = bookingController.getSeatsFull(1L, 12345L);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(bookingService, times(1)).getSeatsFull(1L, 12345L);
    }

    @Test
    void useBooking_shouldCallServiceMethod() {
        // Act
        bookingController.useBooking("BK-12345678");

        // Assert
        verify(bookingService, times(1)).useBooking("BK-12345678");
    }

    @Test
    void cancelBooking_shouldCallServiceMethod() {
        // Act
        bookingController.cancelBooking(10L);

        // Assert
        verify(bookingService, times(1)).cancelBooking(10L);
    }
}

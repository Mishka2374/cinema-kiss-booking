package ru.kisscinema.booking.booking.model;

import jakarta.persistence.*;
import lombok.*;
import ru.kisscinema.booking.common.BaseAuditableEntity;
import ru.kisscinema.booking.session.model.Session;
import ru.kisscinema.booking.hall.model.Seat;

//Бронирование кино
@Entity
@Table(name = "booking", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"seat_id", "session_id"}),
        @UniqueConstraint(columnNames = "booking_code")
})
@Getter
@Setter
@NoArgsConstructor
public class Booking extends BaseAuditableEntity {
    @ManyToOne
    @JoinColumn(name = "session_id", nullable = false)
    private Session session;

    @ManyToOne
    @JoinColumn(name = "seat_id", nullable = false)
    private Seat seat;

    @Column(name = "booking_code", nullable = false, unique = true)
    private String bookingCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status = BookingStatus.RESERVED;
}
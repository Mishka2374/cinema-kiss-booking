package ru.kisscinema.booking.booking.model;

import jakarta.persistence.*;
import lombok.*;
import ru.kisscinema.booking.common.BaseAuditableEntity;
import ru.kisscinema.booking.session.model.Session;
import ru.kisscinema.booking.hall.model.Seat;

@Entity
@Table(name = "booking", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"seat_id", "session_id"})
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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status = BookingStatus.RESERVED;

    private Long chatId;
}
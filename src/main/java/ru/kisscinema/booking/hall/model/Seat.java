package ru.kisscinema.booking.hall.model;

import jakarta.persistence.*;
import lombok.*;
import ru.kisscinema.booking.common.BaseAuditableEntity;

@Entity
@Table(name = "seat")
@Getter
@Setter
@NoArgsConstructor
public class Seat extends BaseAuditableEntity {
    @ManyToOne
    @JoinColumn(name = "row_id", nullable = false)
    private Row row;

    @Column(nullable = false)
    private Integer seatNumber;
}
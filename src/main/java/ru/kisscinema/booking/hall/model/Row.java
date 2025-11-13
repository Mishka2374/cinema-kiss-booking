package ru.kisscinema.booking.hall.model;

import jakarta.persistence.*;
import lombok.*;
import ru.kisscinema.booking.common.BaseAuditableEntity;

//Ряд
@Entity
@Table(name = "row")
@Getter
@Setter
@NoArgsConstructor
public class Row extends BaseAuditableEntity {
    @ManyToOne
    @JoinColumn(name = "hall_id", nullable = false)
    private Hall hall;

    @Column(nullable = false)
    private Integer rowNumber;
}
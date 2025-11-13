package ru.kisscinema.booking.hall.model;

import jakarta.persistence.*;
import lombok.*;
import ru.kisscinema.booking.common.BaseAuditableEntity;

//Кинозал
@Entity
@Table(name = "hall")
@Getter
@Setter
@NoArgsConstructor
public class Hall extends BaseAuditableEntity {
    @Column(nullable = false)
    private String name;
    private String description;
}
package ru.kisscinema.booking.movie.model;

import jakarta.persistence.*;
import lombok.*;
import ru.kisscinema.booking.common.BaseAuditableEntity;

@Entity
@Table(name = "movie")
@Getter
@Setter
@NoArgsConstructor
public class Movie extends BaseAuditableEntity {
    @Column(nullable = false)
    private String title;
    @Column(nullable = false)
    private Integer durationMinutes;
    private String description;
}
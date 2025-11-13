package ru.kisscinema.booking.session.model;

import jakarta.persistence.*;
import lombok.*;
import ru.kisscinema.booking.common.BaseAuditableEntity;
import ru.kisscinema.booking.movie.model.Movie;
import ru.kisscinema.booking.hall.model.Hall;

import java.math.BigDecimal;
import java.time.LocalDateTime;

//Сеансы
@Entity
@Table(name = "session")
@Getter
@Setter
@NoArgsConstructor
public class Session extends BaseAuditableEntity {
    @ManyToOne
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movie;

    @ManyToOne
    @JoinColumn(name = "hall_id", nullable = false)
    private Hall hall;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
}
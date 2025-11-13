package ru.kisscinema.booking.audit.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Фиксирование работы по изменению данных в таблицах: удаление, добавление, обновление
 */
@Entity
@Table(name = "audit_log")
@Getter
@Setter
@NoArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String entityType; // "Hall", "Movie", "Session", "Booking"

    @Column(nullable = false)
    private Long entityId;

    @Column(nullable = false)
    private String action; // "CREATE", "UPDATE", "DELETE"

    @Column(nullable = false)
    private String author; // "admin", "user"

    @Column(nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now();

    @Column(columnDefinition = "TEXT")
    private String details; // JSON или текстовое описание
}

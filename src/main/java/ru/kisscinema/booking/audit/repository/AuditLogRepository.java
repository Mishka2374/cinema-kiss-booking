package ru.kisscinema.booking.audit.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.kisscinema.booking.audit.model.AuditLog;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {}
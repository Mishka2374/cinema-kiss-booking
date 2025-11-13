package ru.kisscinema.booking.audit.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.kisscinema.booking.audit.model.AuditLog;
import ru.kisscinema.booking.audit.repository.AuditLogRepository;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public void log(String entityType, Long entityId, String action, String author, String details) {
        AuditLog log = new AuditLog();
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setAction(action);
        log.setAuthor(author);
        log.setDetails(details);
        auditLogRepository.save(log);
    }
}
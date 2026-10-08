package org.dev.eclipse.service.impl;

import org.dev.eclipse.elasticsearch.AuditLogDocument;
import org.dev.eclipse.elasticsearch.AuditLogRepository;
import org.dev.eclipse.service.AuditLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AuditLogServiceImpl implements AuditLogService {

    private static final Logger logger = LoggerFactory.getLogger(AuditLogServiceImpl.class);
    private final AuditLogRepository auditLogRepository;

    public AuditLogServiceImpl(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Override
    public void log(String username, String action, String resource, String details, String ipAddress) {
        AuditLogDocument log = new AuditLogDocument(
                username,
                action,
                resource,
                details,
                ipAddress
        );

        try {
            auditLogRepository.save(log);
        } catch (Exception ex) {
            logger.warn("Audit log could not be persisted. action={}, resource={}, user={}. Reason: {}",
                    action, resource, username, ex.getMessage());
        }
    }
}

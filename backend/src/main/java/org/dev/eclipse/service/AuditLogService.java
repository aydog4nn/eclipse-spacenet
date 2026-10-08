package org.dev.eclipse.service;

public interface AuditLogService {
    void log(String username, String action, String resource, String details, String ipAddress);
}

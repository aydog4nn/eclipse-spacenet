package org.dev.eclipse.service;


import java.util.UUID;
public interface SecurityService {
    UUID getAuthenticatedUserId();
}

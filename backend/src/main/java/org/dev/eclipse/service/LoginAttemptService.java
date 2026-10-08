package org.dev.eclipse.service;

public interface LoginAttemptService {
    void loginSucceeded(String email);
    void loginFailed(String email);
    boolean isBlocked(String email);
}


package org.dev.eclipse.service.impl;

import org.dev.eclipse.service.LoginAttemptService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class LoginAttemptServiceImpl implements LoginAttemptService {

    private final RedisTemplate<String, String> redisTemplate;

    @Value("${app.auth.login-attempt.max-attempts}")
    private int maxAttempts;

    @Value("${app.auth.login-attempt.block-duration-minutes}")
    private long blockDurationMinutes;

    public LoginAttemptServiceImpl(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    private String key(String email) {
        return "login_attempts:" + email.toLowerCase();
    }

    @Override
    public void loginSucceeded(String email) {
        redisTemplate.delete(key(email));
    }

    @Override
    public void loginFailed(String email) {
        String redisKey = key(email);
        Long attempts = redisTemplate.opsForValue().increment(redisKey);
        redisTemplate.expire(redisKey, blockDurationMinutes, TimeUnit.MINUTES);
    }

    @Override
    public boolean isBlocked(String email) {
        String attempts = redisTemplate.opsForValue().get(key(email));
        return attempts != null && Integer.parseInt(attempts) >= maxAttempts;
    }
}


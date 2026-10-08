package org.dev.eclipse.service.impl;

import org.dev.eclipse.annotation.Audit;
import org.dev.eclipse.service.TokenBlacklistService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

@Service
public class TokenBlacklistServiceImpl implements TokenBlacklistService {

    private static final Logger logger = LoggerFactory.getLogger(TokenBlacklistServiceImpl.class);
    private static final String BLACKLISTED_VALUE = "blacklisted";

    private final RedisTemplate<String, String> redisTemplate;

    public TokenBlacklistServiceImpl(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    @Audit(action = "BLACKLIST_TOKEN", resource = "Token")
    public void blacklistToken(String token, long expirationMillis) {
        String tokenHash = hashToken(token);
        logger.info("Blacklisting token (hash: {}) with expiration in ms: {}", tokenHash, expirationMillis);
        redisTemplate.opsForValue().set(tokenHash, BLACKLISTED_VALUE, expirationMillis, TimeUnit.MILLISECONDS);
    }

    @Override
    public boolean isTokenBlacklisted(String token) {
        String tokenHash = hashToken(token);
        return redisTemplate.hasKey(tokenHash);
    }


    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            logger.error("Failed to hash token for blacklisting: SHA-256 algorithm not available", e);
            throw new RuntimeException("Failed to hash token for blacklisting: SHA-256 algorithm not available", e);
        }
    }
}

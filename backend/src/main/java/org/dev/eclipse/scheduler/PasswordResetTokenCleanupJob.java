package org.dev.eclipse.scheduler;

import jakarta.transaction.Transactional;
import org.dev.eclipse.repository.PasswordResetTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class PasswordResetTokenCleanupJob {

    private static final Logger logger = LoggerFactory.getLogger(PasswordResetTokenCleanupJob.class);
    private static final long TOKEN_LIFETIME_MINUTES = 15L;
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    public PasswordResetTokenCleanupJob(PasswordResetTokenRepository passwordResetTokenRepository) {
        this.passwordResetTokenRepository = passwordResetTokenRepository;
    }

    @Scheduled(fixedDelay = 60000, initialDelay = 60000)
    @Transactional
    public void cleanUpExpiredAndUsedTokens() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiryThreshold = now.minusMinutes(TOKEN_LIFETIME_MINUTES);
        logger.info("Şifre sıfırlama token temizliği başlatıldı: {}", now);

        int deletedCount = passwordResetTokenRepository.deleteExpiredOrUsedTokens(expiryThreshold);

        logger.info("Şifre sıfırlama token temizliği tamamlandı. Silinen kayıt sayısı: {}", deletedCount);
    }
}

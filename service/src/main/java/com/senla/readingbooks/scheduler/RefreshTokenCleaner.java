package com.senla.readingbooks.scheduler;

import com.senla.readingbooks.repository.jpa.user.TokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class RefreshTokenCleaner {
    private final TokenRepository tokenRepository;

    @Transactional
    @Scheduled(cron = "0 0 */6 * * ?")
    public void deleteExpiredTokens() {
        tokenRepository.deleteExpiredTokens(Instant.now());
    }

}
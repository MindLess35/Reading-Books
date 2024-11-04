package com.senla.readingbooks.scheduler;

import com.senla.readingbooks.property.CacheProperty;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JtiBlackListCleaner {
    private final RedisTemplate<String, String> redisTemplate;
    private final CacheProperty cacheProperty;

    @Scheduled(cron = "0 0 0 * * ?")
    public void cleanJtiBlackList() {
        redisTemplate.opsForZSet().removeRangeByScore(cacheProperty.getJtiBlackListName(), 0, System.currentTimeMillis());
    }
}
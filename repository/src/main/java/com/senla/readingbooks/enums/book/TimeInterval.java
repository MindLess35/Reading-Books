package com.senla.readingbooks.enums.book;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@RequiredArgsConstructor
public enum TimeInterval {
    TODAY("today", 1),
    YESTERDAY("yesterday", 2),
    SEVEN_DAYS("seven_days", 7),
    FOURTEEN_DAYS("fourteen_days", 14),
    THIRTY_DAYS("thirty_days", 30);

    private final String value;

    private final int days;

    public Instant getStartDate() {
        return Instant.now().minus(days, ChronoUnit.DAYS);
    }

    @JsonCreator
    public static TimeInterval fromValue(String value) {
        for (TimeInterval interval : TimeInterval.values()) {
            if (interval.value.equalsIgnoreCase(value)) {
                return interval;
            }
        }
        throw new IllegalArgumentException("Unknown time interval: " + value);
    }
}

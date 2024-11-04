package com.senla.readingbooks.property;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "cache")
public class CacheProperty {
    public static final String CACHE_SEPARATOR = "::";

    @NotBlank
    private String jtiBlackListName;

    @NotNull
    @Positive
    private int ttl;

}
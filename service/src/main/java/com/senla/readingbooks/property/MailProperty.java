package com.senla.readingbooks.property;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.Properties;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "spring.mail")
public class MailProperty {

    @NotBlank
    private String host;

    @Min(1)
    private int port;

    @NotBlank
    private String username;

    @NotBlank
    private String password;

    @NotNull
    private Properties properties;

}

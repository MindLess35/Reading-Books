package com.senla.readingbooks.config;

import com.senla.readingbooks.property.MailProperty;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@Configuration
@RequiredArgsConstructor
public class MailConfig {

    private final MailProperty mailProperty;

    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(mailProperty.getHost());
        mailSender.setPort(mailProperty.getPort());
        mailSender.setUsername(mailProperty.getUsername());
        mailSender.setPassword(mailProperty.getPassword());
        mailSender.setJavaMailProperties(mailProperty.getProperties());

        return mailSender;
    }


}

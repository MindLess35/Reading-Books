package com.senla.readingbooks;

import com.senla.readingbooks.entity.user.User;
import com.senla.readingbooks.enums.user.Role;
import com.senla.readingbooks.repository.jpa.user.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO;

@EnableCaching
@EnableScheduling
@ConfigurationPropertiesScan
@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)
@SpringBootApplication
public class ReadingBookApplication {
    public static void main(String[] args) {
        SpringApplication.run(ReadingBookApplication.class, args);
    }

    @Bean
    public CommandLineRunner createModerator(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.findByRole(Role.MODERATOR).isEmpty()) {
                User moderator = User.builder()
                        .username("moderator")
                        .email("moderator@gmail.com")
                        .password(passwordEncoder.encode("Password1@"))
                        .role(Role.MODERATOR)
                        .build();
                userRepository.save(moderator);
            }
        };
    }

    @Bean
    public CommandLineRunner clearRedisCache(RedisTemplate<String, Object> redisTemplate) {
        return args -> redisTemplate.getConnectionFactory().getConnection().serverCommands().flushAll();
    }
}
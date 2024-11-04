package com.senla.readingbooks.config;

import com.senla.readingbooks.enums.user.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.web.filter.OncePerRequestFilter;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final OncePerRequestFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;
    private final LogoutHandler logoutHandler;
    private final AuthenticationEntryPoint authenticationEntryPoint;
    private final AccessDeniedHandler accessDeniedHandler;
    private static final String CHAPTERS = "/api/v1/chapters/**";
    private static final String BOOKS = "/api/v1/books/**";
    private static final String SERIES = "/api/v1/series/**";
    private static final String COLLECTIONS = "/api/v1/collections/**";
    private static final String REVIEWS = "/api/v1/book-reviews/**";
    private static final String USERS = "/api/v1/users/**";
    private static final String LIBRARIES = "/api/v1/libraries/**";
    private static final String COMMENTS = "/api/v1/comments/**";
    private static final String[] WHITE_LIST_URLS = {
            "/api/v1/auth/sign-up",
            "/api/v1/auth/sign-in",
            "/api/v1/auth/refresh",
            "/swagger-ui/**",
            "/v3/api-docs",
            "/v3/api-docs/swagger-config"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .anonymous(AbstractHttpConfigurer::disable);

        String author = Role.AUTHOR.name();
        String moderator = Role.MODERATOR.name();
        httpSecurity.authorizeHttpRequests(request -> request
                .requestMatchers(WHITE_LIST_URLS).permitAll()
                .requestMatchers(HttpMethod.GET,
                        CHAPTERS,
                        BOOKS,
                        SERIES,
                        COLLECTIONS,
                        REVIEWS,
                        USERS,
                        LIBRARIES,
                        COMMENTS).permitAll()

                .requestMatchers(HttpMethod.PUT, BOOKS).hasAnyAuthority(author, moderator)
                .requestMatchers(HttpMethod.PATCH, BOOKS).hasAnyAuthority(author, moderator)
                .requestMatchers(HttpMethod.DELETE, BOOKS).hasAnyAuthority(author, moderator)

                .requestMatchers(HttpMethod.POST, CHAPTERS).hasAnyAuthority(author, moderator)
                .requestMatchers(HttpMethod.PUT, CHAPTERS).hasAnyAuthority(author, moderator)
                .requestMatchers(HttpMethod.PATCH, CHAPTERS).hasAnyAuthority(author, moderator)
                .requestMatchers(HttpMethod.DELETE, CHAPTERS).hasAnyAuthority(author, moderator)

                .requestMatchers(HttpMethod.POST, SERIES).hasAnyAuthority(author, moderator)
                .requestMatchers(HttpMethod.PUT, SERIES).hasAnyAuthority(author, moderator)
                .requestMatchers(HttpMethod.PATCH, SERIES).hasAnyAuthority(author, moderator)
                .requestMatchers(HttpMethod.DELETE, SERIES).hasAnyAuthority(author, moderator)

                .anyRequest().authenticated());


        httpSecurity.sessionManagement(session -> session.sessionCreationPolicy(STATELESS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        httpSecurity.exceptionHandling(ex -> ex
                .authenticationEntryPoint(authenticationEntryPoint)
                .accessDeniedHandler(accessDeniedHandler));


        httpSecurity.logout(logout -> logout
                .logoutUrl("/api/v1/auth/logout")
                .addLogoutHandler(logoutHandler)
                .logoutSuccessHandler((request, response, authentication) ->
                        SecurityContextHolder.clearContext())
        );
        return httpSecurity.build();
    }
}

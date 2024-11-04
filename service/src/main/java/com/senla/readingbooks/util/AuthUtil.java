package com.senla.readingbooks.util;

import com.senla.readingbooks.enums.user.Role;
import lombok.experimental.UtilityClass;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;

@UtilityClass
public class AuthUtil {

    public Long getAuthenticatedUserId() {
        return Optional.ofNullable(getAuthentication())
                .map(Authentication::getPrincipal)
                .map(String.class::cast)
                .map(Long::valueOf)
                .orElseThrow(() -> new RuntimeException("There is no principal (user id) in authentication, but an attempt was made to get it"));

    }

    public boolean isUserAlreadyAuthenticated() {
        return Optional.ofNullable(getAuthentication())
                .map(Authentication::isAuthenticated)
                .orElse(false);
    }

    public Role getAuthenticatedUserRole() {
        return Optional.ofNullable(getAuthentication())
                .map(Authentication::getAuthorities)
                .map(Collection::iterator)
                .map(Iterator::next)
                .map(GrantedAuthority::getAuthority)
                .map(Role::valueOf)
                .orElseThrow(() -> new RuntimeException("There is no role in authentication, but an attempt was made to get it"));

    }

    private Authentication getAuthentication() {
        return Optional.of(SecurityContextHolder.getContext())
                .map(SecurityContext::getAuthentication)
                .orElse(null);
    }
}

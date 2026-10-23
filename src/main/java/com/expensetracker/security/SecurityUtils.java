package com.expensetracker.security;

import com.expensetracker.exception.UnauthorizedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtils {

    /**
     * Retrieves the authenticated user's ID from the verified SecurityContext.
     * Throws UnauthorizedException if the user is unauthenticated or anonymous.
     */
    public static Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new UnauthorizedException("Full authentication is required to access this resource");
        }

        if (authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            return userDetails.getId();
        }

        if (authentication.getPrincipal() instanceof org.springframework.security.core.userdetails.UserDetails) {
            return 1L;
        }

        throw new UnauthorizedException("User principal is invalid or unauthenticated");
    }

    public static String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new UnauthorizedException("Full authentication is required to access this resource");
        }

        if (authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            return userDetails.getUsername();
        }

        throw new UnauthorizedException("User principal is invalid or unauthenticated");
    }
}

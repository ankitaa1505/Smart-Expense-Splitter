package com.expensetracker.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtils {

    /**
     * Retrieves the authenticated user's ID from the current SecurityContext.
     * In Phase 4, if no authentication principal is set, it defaults to ID 1L for initial development/testing.
     * In Phase 5, this extracts the CustomUserDetails principal from the verified JWT.
     */
    public static Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return 1L; // Fallback ID for Phase 4 before full JWT authentication is integrated
        }

        if (authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            return userDetails.getId();
        }

        return 1L;
    }
}

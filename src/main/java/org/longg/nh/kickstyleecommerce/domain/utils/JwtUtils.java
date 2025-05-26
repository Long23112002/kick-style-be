package org.longg.nh.kickstyleecommerce.domain.utils;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@UtilityClass
@Slf4j
public class JwtUtils {

    /**
     * Extract Bearer token from Authorization header
     */
    public static String extractTokenFromHeader(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    /**
     * Validate Authorization header format
     */
    public static boolean isValidAuthHeader(String authHeader) {
        return authHeader != null && authHeader.startsWith("Bearer ") && authHeader.length() > 7;
    }

    /**
     * Create Authorization header value
     */
    public static String createAuthHeader(String token) {
        return "Bearer " + token;
    }

    /**
     * Mask token for logging (show only first 20 characters)
     */
    public static String maskToken(String token) {
        if (token == null || token.length() <= 20) {
            return "***";
        }
        return token.substring(0, 20) + "...";
    }

    /**
     * Check if token looks like a JWT (has 3 parts separated by dots)
     */
    public static boolean isJwtFormat(String token) {
        if (token == null || token.trim().isEmpty()) {
            return false;
        }
        
        String[] parts = token.split("\\.");
        return parts.length == 3;
    }
} 
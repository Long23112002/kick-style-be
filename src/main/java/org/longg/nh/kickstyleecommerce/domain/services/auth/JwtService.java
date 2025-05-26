package org.longg.nh.kickstyleecommerce.domain.services.auth;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.longg.nh.kickstyleecommerce.domain.entities.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class JwtService {

    @Value("${app.jwt.secret:mySecretKey12345678901234567890123456789012345678901234567890}")
    private String jwtSecret;

    @Value("${app.jwt.access-token-expiration:86400}") // 24 hours in seconds
    private Long accessTokenExpiration;

    @Value("${app.jwt.verification-token-expiration:3600}") // 1 hour in seconds
    private Long verificationTokenExpiration;

    /**
     * Generate JWT access token for user
     */
    public String generateAccessToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("email", user.getEmail());
        claims.put("fullName", user.getFullName());
        claims.put("role", user.getRole() != null ? user.getRole().getName() : "USER");
        claims.put("isVerified", user.getIsVerify());
        claims.put("tokenType", "ACCESS");
        
        return generateToken(claims, user.getEmail(), accessTokenExpiration);
    }

    /**
     * Generate JWT verification token for email verification
     */
    public String generateVerificationToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("email", user.getEmail());
        claims.put("tokenType", "VERIFICATION");
        claims.put("purpose", "EMAIL_VERIFICATION");
        
        return generateToken(claims, user.getEmail(), verificationTokenExpiration);
    }

    /**
     * Generate JWT reset password token
     */
    public String generateResetPasswordToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("email", user.getEmail());
        claims.put("tokenType", "RESET_PASSWORD");
        claims.put("purpose", "PASSWORD_RESET");
        
        return generateToken(claims, user.getEmail(), verificationTokenExpiration);
    }

    /**
     * Generate JWT token with claims
     */
    private String generateToken(Map<String, Object> claims, String subject, Long expiration) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration * 1000);

        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Extract email from JWT token
     */
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extract user ID from JWT token
     */
    public Long extractUserId(String token) {
        return extractClaim(token, claims -> claims.get("userId", Long.class));
    }

    /**
     * Extract role from JWT token
     */
    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    /**
     * Extract token type from JWT token
     */
    public String extractTokenType(String token) {
        return extractClaim(token, claims -> claims.get("tokenType", String.class));
    }

    /**
     * Extract expiration date from JWT token
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extract specific claim from JWT token
     */
    public <T> T extractClaim(String token, ClaimsResolver<T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.resolve(claims);
    }

    /**
     * Extract all claims from JWT token
     */
    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException e) {
            log.error("Failed to parse JWT token: {}", e.getMessage());
            throw new RuntimeException("Invalid JWT token");
        }
    }

    /**
     * Check if JWT token is expired
     */
    public boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Validate JWT token
     */
    public boolean isTokenValid(String token, String email) {
        try {
            final String tokenEmail = extractEmail(token);
            return (tokenEmail.equals(email) && !isTokenExpired(token));
        } catch (Exception e) {
            log.error("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Validate access token specifically
     */
    public boolean isValidAccessToken(String token) {
        try {
            String tokenType = extractTokenType(token);
            return "ACCESS".equals(tokenType) && !isTokenExpired(token);
        } catch (Exception e) {
            log.error("Access token validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Validate verification token specifically
     */
    public boolean isValidVerificationToken(String token) {
        try {
            String tokenType = extractTokenType(token);
            return ("VERIFICATION".equals(tokenType) || "RESET_PASSWORD".equals(tokenType)) 
                    && !isTokenExpired(token);
        } catch (Exception e) {
            log.error("Verification token validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Get signing key for JWT
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    /**
     * Get access token expiration in seconds
     */
    public Long getAccessTokenExpirationInSeconds() {
        return accessTokenExpiration;
    }

    /**
     * Get verification token expiration in seconds
     */
    public Long getVerificationTokenExpirationInSeconds() {
        return verificationTokenExpiration;
    }

    /**
     * Convert Date to LocalDateTime
     */
    public LocalDateTime convertToLocalDateTime(Date date) {
        return Instant.ofEpochMilli(date.getTime())
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }

    /**
     * Claims resolver functional interface
     */
    @FunctionalInterface
    public interface ClaimsResolver<T> {
        T resolve(Claims claims);
    }
} 
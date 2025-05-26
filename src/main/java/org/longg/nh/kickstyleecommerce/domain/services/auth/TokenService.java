package org.longg.nh.kickstyleecommerce.domain.services.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.longg.nh.kickstyleecommerce.domain.entities.AccessToken;
import org.longg.nh.kickstyleecommerce.domain.entities.User;
import org.longg.nh.kickstyleecommerce.domain.repositories.AccessTokenRepository;
import org.longg.nh.kickstyleecommerce.domain.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenService {
    
    private final AccessTokenRepository accessTokenRepository;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    
    /**
     * Generate access token cho user (JWT)
     */
    @Transactional
    public AccessToken generateAccessToken(User user) {
        // Revoke all existing tokens for this user
        revokeAllUserTokens(user);
        
        // Generate JWT token
        String jwtToken = jwtService.generateAccessToken(user);
        
        // Calculate expiration time
        Date jwtExpiration = jwtService.extractExpiration(jwtToken);
        Timestamp expirationTime = new Timestamp(jwtExpiration.getTime());
        
        AccessToken accessToken = AccessToken.builder()
                .token(jwtToken)
                .user(user)
                .expiredAt(expirationTime)
                .revoked(false)
                .build();
        
        return accessTokenRepository.save(accessToken);
    }
    
    /**
     * Generate verification token cho email verification (JWT) - sử dụng AccessToken
     */
    @Transactional
    public AccessToken generateVerificationToken(User user) {
        // Generate JWT verification token
        String jwtToken = jwtService.generateVerificationToken(user);
        
        // Calculate expiration time
        Date jwtExpiration = jwtService.extractExpiration(jwtToken);
        Timestamp expirationTime = new Timestamp(jwtExpiration.getTime());
        
        AccessToken verificationToken = AccessToken.builder()
                .token(jwtToken)
                .user(user)
                .expiredAt(expirationTime)
                .revoked(false)
                .build();
        
        return accessTokenRepository.save(verificationToken);
    }

    /**
     * Generate reset password token (JWT) - sử dụng AccessToken
     */
    @Transactional
    public AccessToken generateResetPasswordToken(User user) {
        // Revoke all existing tokens for this user for security
        revokeAllUserTokens(user);
        
        // Generate JWT reset password token
        String jwtToken = jwtService.generateResetPasswordToken(user);
        
        // Calculate expiration time
        Date jwtExpiration = jwtService.extractExpiration(jwtToken);
        Timestamp expirationTime = new Timestamp(jwtExpiration.getTime());
        
        AccessToken resetToken = AccessToken.builder()
                .token(jwtToken)
                .user(user)
                .expiredAt(expirationTime)
                .revoked(false)
                .build();
        
        return accessTokenRepository.save(resetToken);
    }
    
    /**
     * Validate access token (JWT + Database check)
     */
    public Optional<AccessToken> validateAccessToken(String token) {
        try {
            // First validate JWT token format and expiration
            if (!jwtService.isValidAccessToken(token)) {
                return Optional.empty();
            }
            
            // Then check if token exists in database and not revoked
            return accessTokenRepository.findByTokenAndRevokedFalse(token)
                    .filter(AccessToken::isValid);
        } catch (Exception e) {
            log.error("Access token validation failed: {}", e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Validate verification token (JWT + Database check) - sử dụng AccessToken
     */
    public Optional<AccessToken> validateVerificationToken(String token) {
        try {
            // First validate JWT token format and expiration
            if (!jwtService.isValidVerificationToken(token)) {
                return Optional.empty();
            }
            
            // Then check if token exists in database and not revoked
            return accessTokenRepository.findByTokenAndRevokedFalse(token)
                    .filter(AccessToken::isValid);
        } catch (Exception e) {
            log.error("Verification token validation failed: {}", e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Get user from JWT token
     */
    public Optional<User> getUserFromToken(String token) {
        try {
            Long userId = jwtService.extractUserId(token);
            return userRepository.findById(userId);
        } catch (Exception e) {
            log.error("Failed to extract user from token: {}", e.getMessage());
            return Optional.empty();
        }
    }
    
    /**
     * Revoke all tokens for a user
     */
    @Transactional
    public void revokeAllUserTokens(User user) {
        accessTokenRepository.revokeAllUserTokens(user);
    }
    
    /**
     * Revoke specific token
     */
    @Transactional
    public void revokeToken(String token) {
        accessTokenRepository.revokeToken(token);
    }
    
    /**
     * Mark verification token as used - revoke token
     */
    @Transactional
    public void markVerificationTokenAsUsed(AccessToken token) {
        token.setRevoked(true);
        accessTokenRepository.save(token);
    }
    
    /**
     * Get expiration time in seconds for access token
     */
    public long getAccessTokenExpirationInSeconds() {
        return jwtService.getAccessTokenExpirationInSeconds();
    }
} 
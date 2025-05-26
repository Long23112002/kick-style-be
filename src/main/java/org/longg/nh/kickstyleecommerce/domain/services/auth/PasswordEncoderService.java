package org.longg.nh.kickstyleecommerce.domain.services.auth;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class PasswordEncoderService {
    
    private final BCryptPasswordEncoder passwordEncoder;
    
    public PasswordEncoderService() {
        this.passwordEncoder = new BCryptPasswordEncoder(12); // strength 12 cho security cao hơn
    }
    
    /**
     * Encode password sử dụng BCrypt
     */
    public String encode(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }
    
    /**
     * Verify password với encoded password
     */
    public boolean matches(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

} 
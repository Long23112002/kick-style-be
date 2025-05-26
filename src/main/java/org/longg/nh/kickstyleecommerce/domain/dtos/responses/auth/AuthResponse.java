package org.longg.nh.kickstyleecommerce.domain.dtos.responses.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.longg.nh.kickstyleecommerce.domain.dtos.responses.UserResponse;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthResponse {
    
    private String accessToken;
    private String tokenType = "Bearer";
    private UserResponse userInfo;
    private Long expiresIn; // seconds
} 
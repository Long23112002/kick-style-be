package org.longg.nh.kickstyleecommerce.domain.dtos.responses.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthResponse {
    
    private String accessToken;
    private String tokenType = "Bearer";
    private UserResponse userInfo;
    private Long expiresIn;
} 
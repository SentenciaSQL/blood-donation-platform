package com.afriasdev.dds.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    /** Access token (kept for backward compatibility with existing clients). */
    private String token;
    private String accessToken;
    private String refreshToken;
    private String role;
    private String email;

    public static AuthResponse of(String accessToken, String refreshToken, String role, String email) {
        return new AuthResponse(accessToken, accessToken, refreshToken, role, email);
    }
}

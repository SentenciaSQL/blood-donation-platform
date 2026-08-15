package com.afriasdev.dds.api;

import com.afriasdev.dds.api.dto.AuthRequest;
import com.afriasdev.dds.api.dto.AuthResponse;
import com.afriasdev.dds.api.dto.RefreshTokenRequest;
import com.afriasdev.dds.api.dto.RegisterRequest;
import com.afriasdev.dds.domain.Role;
import com.afriasdev.dds.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerUnitTest {

    @Mock
    AuthService authService;

    AuthController controller;

    @BeforeEach
    void setUp() {
        controller = new AuthController(authService);
    }

    @Test
    void register_delegates_to_service_and_returns_token() {
        var req = new RegisterRequest();
        req.setFirstName("Andres");
        req.setLastName("Frias");
        req.setEmail("a@test.com");
        req.setPassword("password123");
        req.setRole(Role.DONOR);
        req.setBloodType("O+");

        when(authService.register(req)).thenReturn(AuthResponse.of("ACCESS", "REFRESH", "DONOR", "a@test.com"));

        ResponseEntity<AuthResponse> resp = controller.register(req);

        assertThat(resp.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().getAccessToken()).isEqualTo("ACCESS");
        assertThat(resp.getBody().getRefreshToken()).isEqualTo("REFRESH");
        verify(authService).register(req);
    }

    @Test
    void login_delegates_to_service_and_returns_token() {
        var authReq = new AuthRequest();
        authReq.setEmail("a@test.com");
        authReq.setPassword("password123");

        when(authService.login(authReq)).thenReturn(AuthResponse.of("ACCESS", "REFRESH", "DONOR", "a@test.com"));

        var resp = controller.login(authReq);

        assertThat(resp.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().getToken()).isEqualTo("ACCESS");
        verify(authService).login(authReq);
    }

    @Test
    void refresh_delegates_to_service() {
        var req = new RefreshTokenRequest();
        req.setRefreshToken("REFRESH");
        when(authService.refresh("REFRESH")).thenReturn(AuthResponse.of("ACCESS2", "REFRESH2", "DONOR", "a@test.com"));

        var resp = controller.refresh(req);

        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().getAccessToken()).isEqualTo("ACCESS2");
        verify(authService).refresh("REFRESH");
    }
}

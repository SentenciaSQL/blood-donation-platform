package com.afriasdev.dds.api;

import com.afriasdev.dds.api.dto.AuthRequest;
import com.afriasdev.dds.api.dto.AuthResponse;
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

        when(authService.register(req)).thenReturn(new AuthResponse("TOKEN", "DONOR", "a@test.com"));

        ResponseEntity<AuthResponse> resp = controller.register(req);

        assertThat(resp.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().getToken()).isEqualTo("TOKEN");
        verify(authService).register(req);
    }

    @Test
    void login_delegates_to_service_and_returns_token() {
        var authReq = new AuthRequest();
        authReq.setEmail("a@test.com");
        authReq.setPassword("password123");

        when(authService.login(authReq)).thenReturn(new AuthResponse("TOKEN", "DONOR", "a@test.com"));

        var resp = controller.login(authReq);

        assertThat(resp.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().getToken()).isEqualTo("TOKEN");
        verify(authService).login(authReq);
    }
}

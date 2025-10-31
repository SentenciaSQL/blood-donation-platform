package com.afriasdev.dds.api;

import com.afriasdev.dds.api.dto.AuthRequest;
import com.afriasdev.dds.api.dto.AuthResponse;
import com.afriasdev.dds.api.dto.RegisterRequest;
import com.afriasdev.dds.domain.Role;
import com.afriasdev.dds.domain.User;
import com.afriasdev.dds.repository.UserRepository;
import com.afriasdev.dds.security.JwtService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthControllerUnitTest {

    @Mock
    AuthenticationManager authManager;

    @Mock
    UserRepository users;

    @Mock
    PasswordEncoder encoder;

    @Mock
    JwtService jwt;

    AuthController controller;

    @BeforeEach
    void setUp() {
        controller = new AuthController(authManager, users, encoder, jwt);
    }

    @Test
    void register_creates_user_and_returns_token() {
        when(users.existsByEmail("a@test.com")).thenReturn(false);
        when(encoder.encode("123456")).thenReturn("hashed");
        when(jwt.generate(eq("a@test.com"), anyMap())).thenReturn("TOKEN");

        var req = new RegisterRequest();
        req.setFirstName("Andres");
        req.setLastName("Frias");
        req.setEmail("a@test.com");
        req.setPassword("123456");
        req.setRole(Role.ADMIN);

        ResponseEntity<?> resp = controller.register(req);

        assertThat(resp.getStatusCode().is2xxSuccessful()).isTrue();
        var body = (AuthResponse) resp.getBody();
        assertThat(body.getToken()).isEqualTo("TOKEN");

        verify(users).save(argThat(u ->
                u.getEmail().equals("a@test.com") &&
                        u.getPassword().equals("hashed") &&
                        u.getRole() == Role.ADMIN
        ));
    }

    @Test
    void login_authenticates_and_returns_token() {
        var authReq = new AuthRequest();
        authReq.setEmail("a@test.com");
        authReq.setPassword("123456");

        var u = new User();
        u.setEmail("a@test.com");
        u.setRole(Role.DONOR);
        when(users.findByEmail("a@test.com")).thenReturn(Optional.of(u));
        when(jwt.generate(eq("a@test.com"), any(Map.class))).thenReturn("TOKEN");
        when(authManager.authenticate(any())).thenReturn(mock(Authentication.class));

        var resp = controller.login(authReq);

        assertThat(resp.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(resp.getBody().getToken()).isEqualTo("TOKEN");
    }

}

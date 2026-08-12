package com.afriasdev.dds.service;

import com.afriasdev.dds.api.dto.AuthRequest;
import com.afriasdev.dds.api.dto.RegisterRequest;
import com.afriasdev.dds.domain.Donor;
import com.afriasdev.dds.domain.RefreshToken;
import com.afriasdev.dds.domain.Role;
import com.afriasdev.dds.domain.User;
import com.afriasdev.dds.exception.BadRequestException;
import com.afriasdev.dds.exception.ConflictException;
import com.afriasdev.dds.repository.DonorRepository;
import com.afriasdev.dds.repository.RefreshTokenRepository;
import com.afriasdev.dds.repository.UserRepository;
import com.afriasdev.dds.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock AuthenticationManager authManager;
    @Mock UserRepository users;
    @Mock DonorRepository donors;
    @Mock RefreshTokenRepository refreshTokens;
    @Mock PasswordEncoder encoder;
    @Mock JwtService jwt;

    @InjectMocks AuthService service;

    @Test
    void register_creates_donor_profile_and_returns_tokens() {
        when(users.existsByEmail("donor@test.com")).thenReturn(false);
        when(encoder.encode("password123")).thenReturn("hashed");
        when(users.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });
        when(donors.save(any(Donor.class))).thenAnswer(inv -> inv.getArgument(0));
        when(jwt.generateAccessToken("donor@test.com", "DONOR")).thenReturn("ACCESS");
        when(jwt.generateRefreshToken("donor@test.com")).thenReturn("REFRESH");
        when(jwt.getRefreshExpirationDays()).thenReturn(7L);
        when(refreshTokens.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));

        var req = new RegisterRequest();
        req.setFirstName("Donor");
        req.setLastName("One");
        req.setEmail("donor@test.com");
        req.setPassword("password123");
        req.setRole(Role.DONOR);
        req.setBloodType("O+");

        var response = service.register(req);

        assertThat(response.getAccessToken()).isEqualTo("ACCESS");
        assertThat(response.getRefreshToken()).isEqualTo("REFRESH");
        assertThat(response.getToken()).isEqualTo("ACCESS");

        ArgumentCaptor<Donor> donorCaptor = ArgumentCaptor.forClass(Donor.class);
        verify(donors).save(donorCaptor.capture());
        assertThat(donorCaptor.getValue().getBloodType().getCode()).isEqualTo("O+");
    }

    @Test
    void register_rejects_admin_self_registration() {
        var req = new RegisterRequest();
        req.setFirstName("Admin");
        req.setLastName("User");
        req.setEmail("admin@test.com");
        req.setPassword("password123");
        req.setRole(Role.ADMIN);

        assertThatThrownBy(() -> service.register(req))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("ADMIN");
    }

    @Test
    void register_rejects_duplicate_email() {
        when(users.existsByEmail("a@test.com")).thenReturn(true);

        var req = new RegisterRequest();
        req.setFirstName("A");
        req.setLastName("B");
        req.setEmail("a@test.com");
        req.setPassword("password123");
        req.setRole(Role.REQUESTER);

        assertThatThrownBy(() -> service.register(req))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void login_returns_tokens() {
        var authReq = new AuthRequest();
        authReq.setEmail("a@test.com");
        authReq.setPassword("password123");

        var user = new User();
        user.setEmail("a@test.com");
        user.setRole(Role.DONOR);
        user.setActive(true);

        when(authManager.authenticate(any())).thenReturn(mock(Authentication.class));
        when(users.findByEmail("a@test.com")).thenReturn(Optional.of(user));
        when(jwt.generateAccessToken(eq("a@test.com"), anyString())).thenReturn("ACCESS");
        when(jwt.generateRefreshToken("a@test.com")).thenReturn("REFRESH");
        when(jwt.getRefreshExpirationDays()).thenReturn(7L);
        when(refreshTokens.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));

        var response = service.login(authReq);

        assertThat(response.getAccessToken()).isEqualTo("ACCESS");
        assertThat(response.getRefreshToken()).isEqualTo("REFRESH");
        verify(refreshTokens).revokeAllForUser(user);
    }
}

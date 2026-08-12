package com.afriasdev.dds.service;

import com.afriasdev.dds.api.dto.AuthRequest;
import com.afriasdev.dds.api.dto.AuthResponse;
import com.afriasdev.dds.api.dto.RegisterRequest;
import com.afriasdev.dds.domain.BloodType;
import com.afriasdev.dds.domain.Donor;
import com.afriasdev.dds.domain.RefreshToken;
import com.afriasdev.dds.domain.Role;
import com.afriasdev.dds.domain.User;
import com.afriasdev.dds.exception.BadRequestException;
import com.afriasdev.dds.exception.ConflictException;
import com.afriasdev.dds.exception.UnauthorizedException;
import com.afriasdev.dds.repository.DonorRepository;
import com.afriasdev.dds.repository.RefreshTokenRepository;
import com.afriasdev.dds.repository.UserRepository;
import com.afriasdev.dds.security.JwtService;
import com.afriasdev.dds.util.BloodTypeValidator;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class AuthService {

    private final AuthenticationManager authManager;
    private final UserRepository users;
    private final DonorRepository donors;
    private final RefreshTokenRepository refreshTokens;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    public AuthService(
            AuthenticationManager authManager,
            UserRepository users,
            DonorRepository donors,
            RefreshTokenRepository refreshTokens,
            PasswordEncoder encoder,
            JwtService jwt
    ) {
        this.authManager = authManager;
        this.users = users;
        this.donors = donors;
        this.refreshTokens = refreshTokens;
        this.encoder = encoder;
        this.jwt = jwt;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (request.getRole() == Role.ADMIN) {
            throw new BadRequestException("Self-registration as ADMIN is not allowed");
        }

        if (users.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email already exists");
        }

        BloodType bloodType = null;
        if (request.getRole() == Role.DONOR) {
            if (request.getBloodType() == null || request.getBloodType().isBlank()) {
                throw new BadRequestException("Blood type is required for donor registration");
            }
            if (!BloodTypeValidator.isValid(request.getBloodType())) {
                throw new BadRequestException("Invalid blood type");
            }
            bloodType = BloodType.fromCode(request.getBloodType());
        }

        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPassword(encoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        user.setPhone(request.getPhone());
        user.setActive(true);
        user.setCreatedAt(Instant.now());
        users.save(user);

        if (request.getRole() == Role.DONOR) {
            Donor donor = Donor.builder()
                    .user(user)
                    .bloodType(bloodType)
                    .phone(request.getPhone())
                    .eligible(true)
                    .active(true)
                    .availability(true)
                    .createdAt(Instant.now())
                    .build();
            donors.save(donor);
        }

        return issueTokens(user);
    }

    @Transactional
    public AuthResponse login(AuthRequest request) {
        var auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        User user = users.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));
        if (Boolean.FALSE.equals(user.getActive())) {
            throw new UnauthorizedException("User account is inactive");
        }

        refreshTokens.revokeAllForUser(user);
        return issueTokens(user);
    }

    @Transactional
    public AuthResponse refresh(String refreshTokenValue) {
        if (refreshTokenValue == null || refreshTokenValue.isBlank()) {
            throw new UnauthorizedException("Refresh token is required");
        }

        RefreshToken stored = refreshTokens.findByTokenAndRevokedFalse(refreshTokenValue)
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

        if (stored.isExpired()) {
            stored.setRevoked(true);
            throw new UnauthorizedException("Refresh token expired");
        }

        try {
            if (!jwt.isRefreshToken(refreshTokenValue)) {
                throw new UnauthorizedException("Invalid refresh token type");
            }
        } catch (UnauthorizedException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new UnauthorizedException("Invalid refresh token");
        }

        User user = stored.getUser();
        if (Boolean.FALSE.equals(user.getActive())) {
            throw new UnauthorizedException("User account is inactive");
        }

        stored.setRevoked(true);
        return issueTokens(user);
    }

    private AuthResponse issueTokens(User user) {
        String accessToken = jwt.generateAccessToken(user.getEmail(), user.getRole().name());
        String refreshTokenValue = jwt.generateRefreshToken(user.getEmail());

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(refreshTokenValue)
                .expiresAt(Instant.now().plus(jwt.getRefreshExpirationDays(), ChronoUnit.DAYS))
                .revoked(false)
                .createdAt(Instant.now())
                .build();
        refreshTokens.save(refreshToken);

        return AuthResponse.of(accessToken, refreshTokenValue, user.getRole().name(), user.getEmail());
    }
}

package com.afriasdev.dds.service;

import com.afriasdev.dds.api.dto.AuthRequest;
import com.afriasdev.dds.api.dto.AuthResponse;
import com.afriasdev.dds.api.dto.RegisterRequest;
import com.afriasdev.dds.domain.Donor;
import com.afriasdev.dds.domain.Role;
import com.afriasdev.dds.domain.User;
import com.afriasdev.dds.exception.BadRequestException;
import com.afriasdev.dds.exception.ConflictException;
import com.afriasdev.dds.exception.ResourceNotFoundException;
import com.afriasdev.dds.repository.DonorRepository;
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
import java.util.Map;

@Service
public class AuthService {

    private final AuthenticationManager authManager;
    private final UserRepository users;
    private final DonorRepository donors;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    public AuthService(
            AuthenticationManager authManager,
            UserRepository users,
            DonorRepository donors,
            PasswordEncoder encoder,
            JwtService jwt
    ) {
        this.authManager = authManager;
        this.users = users;
        this.donors = donors;
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

        if (request.getRole() == Role.DONOR) {
            if (request.getBloodType() == null || request.getBloodType().isBlank()) {
                throw new BadRequestException("Blood type is required for donor registration");
            }
            if (!BloodTypeValidator.isValid(request.getBloodType())) {
                throw new BadRequestException("Invalid blood type");
            }
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
                    .bloodType(request.getBloodType())
                    .availability(true)
                    .build();
            donors.save(donor);
        }

        return buildAuthResponse(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(AuthRequest request) {
        var auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        User user = users.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return buildAuthResponse(user);
    }

    private AuthResponse buildAuthResponse(User user) {
        String token = jwt.generate(user.getEmail(), Map.of("role", user.getRole().name()));
        return new AuthResponse(token, user.getRole().name(), user.getEmail());
    }
}

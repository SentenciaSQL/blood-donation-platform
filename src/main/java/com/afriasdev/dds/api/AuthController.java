package com.afriasdev.dds.api;

import com.afriasdev.dds.api.dto.AuthRequest;
import com.afriasdev.dds.api.dto.AuthResponse;
import com.afriasdev.dds.api.dto.RegisterRequest;
import com.afriasdev.dds.domain.User;
import com.afriasdev.dds.repository.UserRepository;
import com.afriasdev.dds.security.JwtService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authManager;
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    public AuthController(AuthenticationManager authManager, UserRepository users, PasswordEncoder encoder, JwtService jwt) {
        this.authManager = authManager;
        this.users = users;
        this.encoder = encoder;
        this.jwt = jwt;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        if (users.existsByEmail(request.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email already exists");
        }

        var user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPassword(encoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        user.setPhone(request.getPhone());
        users.save(user);

        String token = jwt.generate(user.getEmail(), Map.of("role", user.getRole().name()));
        return ResponseEntity.ok(new AuthResponse(token, user.getRole().name(), user.getEmail()));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        var auth = authManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(auth);
        var u = users.findByEmail(request.getEmail()).orElseThrow();
        String token = jwt.generate(u.getEmail(), Map.of("role", u.getRole().name()));
        return ResponseEntity.ok(new AuthResponse(token, u.getRole().name(), u.getEmail()));
    }


}
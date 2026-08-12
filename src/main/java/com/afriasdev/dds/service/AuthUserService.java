package com.afriasdev.dds.service;

import com.afriasdev.dds.domain.User;
import com.afriasdev.dds.exception.UnauthorizedException;
import com.afriasdev.dds.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthUserService {

    private final UserRepository users;

    public AuthUserService(UserRepository users) {
        this.users = users;
    }

    @Transactional(readOnly = true)
    public User currentUser(Authentication auth) {
        if (auth == null || auth.getName() == null) {
            throw new UnauthorizedException("Authentication required");
        }
        return users.findByEmail(auth.getName())
                .orElseThrow(() -> new UnauthorizedException("Authenticated user not found"));
    }
}

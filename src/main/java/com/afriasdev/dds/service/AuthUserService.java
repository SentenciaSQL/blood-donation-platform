package com.afriasdev.dds.service;

import com.afriasdev.dds.domain.User;
import com.afriasdev.dds.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class AuthUserService {

    private final UserRepository users;

    public AuthUserService(UserRepository users) {
        this.users = users;
    }

    public User currentUser(Authentication auth) {
        return users.findByEmail(auth.getName()).orElseThrow();
    }
}

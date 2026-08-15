package com.afriasdev.dds.service;

import com.afriasdev.dds.domain.User;
import com.afriasdev.dds.exception.ResourceNotFoundException;
import com.afriasdev.dds.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository users;

    public UserService(UserRepository users) {
        this.users = users;
    }

    @Transactional(readOnly = true)
    public User getById(Long id) {
        return users.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}

package com.afriasdev.dds.security;

import com.afriasdev.dds.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository repo;
    public CustomUserDetailsService(UserRepository repo) {
        this.repo = repo;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        var u = repo.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        var auth = org.springframework.security.core.authority.AuthorityUtils.createAuthorityList("ROLE_" + u.getRole().name());
        return new org.springframework.security.core.userdetails.User(u.getEmail(), u.getPassword(), u.getActive(), true, true, true, auth);
    };
}

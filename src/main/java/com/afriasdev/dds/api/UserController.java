package com.afriasdev.dds.api;

import com.afriasdev.dds.api.dto.user.UserSummaryDto;
import com.afriasdev.dds.api.mapper.EntityMapper;
import com.afriasdev.dds.service.AuthUserService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserController {

    private final AuthUserService authUserService;
    private final EntityMapper mapper;

    public UserController(AuthUserService authUserService, EntityMapper mapper) {
        this.authUserService = authUserService;
        this.mapper = mapper;
    }

    @GetMapping("/me")
    public UserSummaryDto me(Authentication authentication) {
        return mapper.toUserSummary(authUserService.currentUser(authentication));
    }
}

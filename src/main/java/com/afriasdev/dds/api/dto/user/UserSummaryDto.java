package com.afriasdev.dds.api.dto.user;

import com.afriasdev.dds.domain.Role;

import java.time.Instant;

public record UserSummaryDto(
        Long id,
        String firstName,
        String lastName,
        String email,
        String phone,
        Role role,
        Boolean active,
        Instant createdAt
) {
}

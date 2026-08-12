package com.afriasdev.dds.api.dto.bank;

import java.math.BigDecimal;
import java.time.Instant;

public record BloodBankResponseDto(
        Long id,
        String name,
        String address,
        String city,
        String phone,
        String email,
        BigDecimal latitude,
        BigDecimal longitude,
        Boolean active,
        Instant createdAt,
        Instant updatedAt
) {
}

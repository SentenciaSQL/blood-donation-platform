package com.afriasdev.dds.api.dto.bank;

import java.math.BigDecimal;
import java.time.Instant;

public record BloodBankResponseDto(
        Long id,
        String name,
        String address,
        String phone,
        BigDecimal latitude,
        BigDecimal longitude,
        Instant createdAt
) {
}

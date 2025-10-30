package com.afriasdev.dds.api.dto.bank;

import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

public record CreateBloodBankDto(
        @NotBlank String name,
        String address,
        String phone,
        BigDecimal latitude,
        BigDecimal longitude
) {
}

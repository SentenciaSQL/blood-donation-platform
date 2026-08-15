package com.afriasdev.dds.api.dto.bank;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record UpdateBloodBankDto(
        @NotBlank @Size(max = 160) String name,
        @Size(max = 240) String address,
        @Size(max = 120) String city,
        @Size(max = 40) String phone,
        @Email @Size(max = 160) String email,
        BigDecimal latitude,
        BigDecimal longitude,
        Boolean active
) {
}

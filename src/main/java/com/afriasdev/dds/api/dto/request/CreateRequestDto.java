package com.afriasdev.dds.api.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

public record CreateRequestDto(
    @NotBlank
    String bloodType,
    @Min(1)
    @Max(3)
    Short urgency,
    String hospital,
    BigDecimal latitude,
    BigDecimal longitude
) { }

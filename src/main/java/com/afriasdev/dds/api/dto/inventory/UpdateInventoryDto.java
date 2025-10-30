package com.afriasdev.dds.api.dto.inventory;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

public record UpdateInventoryDto(
    @NotBlank
    String bloodType,
    @PositiveOrZero
    Integer unitsAvailable
) { }

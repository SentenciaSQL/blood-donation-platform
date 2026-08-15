package com.afriasdev.dds.api.dto.inventory;

import com.afriasdev.dds.validation.ValidBloodType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record CreateInventoryDto(
        @NotNull Long bloodBankId,
        @NotBlank @ValidBloodType String bloodType,
        @NotNull @PositiveOrZero Integer unitsAvailable,
        @PositiveOrZero Integer minimumStock
) {
}

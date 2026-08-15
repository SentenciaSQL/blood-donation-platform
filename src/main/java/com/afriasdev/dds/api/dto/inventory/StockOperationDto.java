package com.afriasdev.dds.api.dto.inventory;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record StockOperationDto(
        @NotNull @Positive Integer units
) {
}

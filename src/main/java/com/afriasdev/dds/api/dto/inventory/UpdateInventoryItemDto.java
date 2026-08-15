package com.afriasdev.dds.api.dto.inventory;

import jakarta.validation.constraints.PositiveOrZero;

public record UpdateInventoryItemDto(
        @PositiveOrZero Integer unitsAvailable,
        @PositiveOrZero Integer minimumStock
) {
}

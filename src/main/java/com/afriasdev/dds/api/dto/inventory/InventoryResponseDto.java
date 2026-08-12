package com.afriasdev.dds.api.dto.inventory;

import java.time.Instant;

public record InventoryResponseDto(
        Long id,
        Long bloodBankId,
        String bloodBankName,
        String bloodType,
        Integer unitsAvailable,
        Integer minimumStock,
        boolean lowStock,
        Instant updatedAt
) {
}

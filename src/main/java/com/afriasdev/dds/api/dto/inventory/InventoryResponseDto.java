package com.afriasdev.dds.api.dto.inventory;

import java.time.Instant;

public record InventoryResponseDto(
        Long id,
        Long bloodBankId,
        String bloodType,
        Integer unitsAvailable,
        Instant updatedAt
) {
}

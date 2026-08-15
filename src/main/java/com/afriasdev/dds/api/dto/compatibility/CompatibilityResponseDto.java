package com.afriasdev.dds.api.dto.compatibility;

import java.util.List;

public record CompatibilityResponseDto(
        String donorType,
        String recipientType,
        boolean compatible,
        List<String> compatibleDonorTypes,
        List<String> compatibleRecipientTypes
) {
}

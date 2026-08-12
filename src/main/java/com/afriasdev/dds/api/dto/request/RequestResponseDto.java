package com.afriasdev.dds.api.dto.request;

import com.afriasdev.dds.api.dto.user.UserSummaryDto;

import java.math.BigDecimal;
import java.time.Instant;

public record RequestResponseDto(
        Long id,
        UserSummaryDto requester,
        String bloodType,
        Short urgency,
        String hospital,
        BigDecimal latitude,
        BigDecimal longitude,
        String status,
        UserSummaryDto matchedDonor,
        Instant createdAt
) {
}

package com.afriasdev.dds.api.dto.donation;

import com.afriasdev.dds.api.dto.bank.BloodBankResponseDto;
import com.afriasdev.dds.api.dto.user.UserSummaryDto;

import java.time.Instant;

public record DonationResponseDto(
        Long id,
        UserSummaryDto donor,
        BloodBankResponseDto bloodBank,
        Long requestId,
        Instant scheduledAt,
        String status,
        Instant createdAt
) {
}

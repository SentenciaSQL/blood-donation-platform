package com.afriasdev.dds.api.dto.donation;

import com.afriasdev.dds.api.dto.bank.BloodBankResponseDto;
import com.afriasdev.dds.api.dto.user.UserSummaryDto;
import com.afriasdev.dds.domain.DonationStatus;

import java.time.Instant;

public record DonationResponseDto(
        Long id,
        UserSummaryDto donor,
        BloodBankResponseDto bloodBank,
        Long requestId,
        Instant appointmentDate,
        DonationStatus status,
        String bloodType,
        Integer unitsCollected,
        String notes,
        Instant createdAt,
        Instant updatedAt
) {
}

package com.afriasdev.dds.api.dto.donor;

import com.afriasdev.dds.api.dto.user.UserSummaryDto;

import java.time.Instant;

public record DonorResponseDto(
        Long userId,
        UserSummaryDto user,
        String bloodType,
        Instant lastDonationAt,
        Boolean availability
) {
}

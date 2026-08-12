package com.afriasdev.dds.api.dto.donor;

import com.afriasdev.dds.api.dto.user.UserSummaryDto;
import com.afriasdev.dds.domain.Gender;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record DonorResponseDto(
        Long id,
        UserSummaryDto user,
        String bloodType,
        LocalDate birthDate,
        Gender gender,
        String phone,
        String city,
        String address,
        BigDecimal latitude,
        BigDecimal longitude,
        BigDecimal weight,
        Instant lastDonationDate,
        Boolean eligible,
        Boolean active,
        Instant createdAt,
        Instant updatedAt
) {
}

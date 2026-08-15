package com.afriasdev.dds.api.dto.request;

import com.afriasdev.dds.api.dto.user.UserSummaryDto;
import com.afriasdev.dds.domain.RequestStatus;
import com.afriasdev.dds.domain.RequestUrgency;

import java.time.Instant;
import java.time.LocalDate;

public record RequestResponseDto(
        Long id,
        UserSummaryDto requester,
        String bloodType,
        Integer unitsRequired,
        String hospitalName,
        String patientName,
        String contactPhone,
        String city,
        String description,
        RequestUrgency urgency,
        RequestStatus status,
        LocalDate requiredDate,
        UserSummaryDto matchedDonor,
        Instant createdAt,
        Instant updatedAt
) {
}

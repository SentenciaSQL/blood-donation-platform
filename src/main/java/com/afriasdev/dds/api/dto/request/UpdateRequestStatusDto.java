package com.afriasdev.dds.api.dto.request;

import com.afriasdev.dds.domain.RequestStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateRequestStatusDto(
        @NotNull RequestStatus status,
        Long matchedDonorUserId
) {
}

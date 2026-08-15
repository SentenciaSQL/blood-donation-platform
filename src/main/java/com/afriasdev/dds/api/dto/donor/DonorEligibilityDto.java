package com.afriasdev.dds.api.dto.donor;

import java.time.Instant;
import java.util.List;

public record DonorEligibilityDto(
        boolean eligible,
        Instant lastDonationDate,
        Instant nextEligibleDate,
        List<String> reasons
) {
}

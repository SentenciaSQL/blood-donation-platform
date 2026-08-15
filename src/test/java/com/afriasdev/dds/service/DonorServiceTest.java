package com.afriasdev.dds.service;

import com.afriasdev.dds.api.dto.donor.UpdateDonorProfileDto;
import com.afriasdev.dds.domain.BloodType;
import com.afriasdev.dds.domain.Donor;
import com.afriasdev.dds.domain.Gender;
import com.afriasdev.dds.domain.Role;
import com.afriasdev.dds.domain.User;
import com.afriasdev.dds.repository.DonationRepository;
import com.afriasdev.dds.repository.DonorRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DonorServiceTest {

    @Mock DonorRepository donors;
    @Mock DonationRepository donations;

    @InjectMocks DonorService service;

    @Test
    void updateMyProfile_recalculates_eligibility() {
        var user = new User();
        user.setId(1L);
        user.setRole(Role.DONOR);

        var donor = Donor.builder()
                .userId(1L)
                .user(user)
                .bloodType(BloodType.O_POS)
                .active(true)
                .eligible(true)
                .build();

        when(donors.findByUserId(1L)).thenReturn(Optional.of(donor));
        when(donors.save(any(Donor.class))).thenAnswer(inv -> inv.getArgument(0));

        var dto = new UpdateDonorProfileDto(
                "A+",
                LocalDate.now().minusYears(30),
                Gender.MALE,
                "8091112222",
                "Santo Domingo",
                "Calle 1",
                null,
                null,
                new BigDecimal("70"),
                true
        );

        var updated = service.updateMyProfile(user, dto);

        assertThat(updated.getBloodType()).isEqualTo(BloodType.A_POS);
        assertThat(updated.getCity()).isEqualTo("Santo Domingo");
        assertThat(updated.getEligible()).isTrue();
    }

    @Test
    void eligibility_fails_for_low_weight() {
        var donor = Donor.builder()
                .active(true)
                .birthDate(LocalDate.now().minusYears(25))
                .weight(new BigDecimal("45"))
                .build();

        var reasons = new java.util.ArrayList<String>();
        boolean eligible = service.evaluateEligibility(donor, reasons);

        assertThat(eligible).isFalse();
        assertThat(reasons).anyMatch(r -> r.contains("Weight"));
    }
}

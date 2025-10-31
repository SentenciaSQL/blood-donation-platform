package com.afriasdev.dds.service;

import com.afriasdev.dds.api.dto.donation.ScheduleDonationDto;
import com.afriasdev.dds.api.dto.donation.UpdateDonationStatusDto;
import com.afriasdev.dds.domain.BloodBank;
import com.afriasdev.dds.domain.Donation;
import com.afriasdev.dds.domain.User;
import com.afriasdev.dds.repository.BloodBankRepository;
import com.afriasdev.dds.repository.DonationRepository;
import com.afriasdev.dds.repository.RequestRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DonationServiceTest {
    @Mock
    DonationRepository donations;

    @Mock
    BloodBankRepository banks;

    @Mock
    RequestRepository requests;

    @InjectMocks
    DonationService service;

    @Test
    void schedule_creates_scheduled_donation() {
        var donor = new User();
        donor.setId(10L);
        var bank = BloodBank.builder().id(2L).build();
        when(banks.findById(2L)).thenReturn(Optional.of(bank));
        when(donations.save(any(Donation.class))).thenAnswer(inv -> inv.getArgument(0));

        var dto = new ScheduleDonationDto(2L, null, Instant.parse("2025-10-30T12:00:00Z"));
        var saved = service.schedule(donor, dto);

        assertThat(saved.getDonor().getId()).isEqualTo(10L);
        assertThat(saved.getBloodBank().getId()).isEqualTo(2L);
        assertThat(saved.getStatus()).isEqualTo("SCHEDULED");
    }

    @Test
    void updateStatus_changes_status() {
        var d = Donation.builder().id(1L).status("SCHEDULED").build();
        when(donations.findById(1L)).thenReturn(Optional.of(d));

        var updated = service.updateStatus(1L, new UpdateDonationStatusDto("COMPLETED"));

        assertThat(updated.getStatus()).isEqualTo("COMPLETED");
    }
}

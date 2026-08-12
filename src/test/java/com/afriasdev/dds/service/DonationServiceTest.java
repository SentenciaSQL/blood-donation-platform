package com.afriasdev.dds.service;

import com.afriasdev.dds.api.dto.donation.ScheduleDonationDto;
import com.afriasdev.dds.domain.BloodBank;
import com.afriasdev.dds.domain.BloodType;
import com.afriasdev.dds.domain.Donation;
import com.afriasdev.dds.domain.DonationStatus;
import com.afriasdev.dds.domain.Donor;
import com.afriasdev.dds.domain.Role;
import com.afriasdev.dds.domain.User;
import com.afriasdev.dds.repository.BloodBankRepository;
import com.afriasdev.dds.repository.DonationRepository;
import com.afriasdev.dds.repository.DonorRepository;
import com.afriasdev.dds.repository.RequestRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DonationServiceTest {

    @Mock DonationRepository donations;
    @Mock BloodBankRepository banks;
    @Mock RequestRepository requests;
    @Mock DonorRepository donors;
    @Mock InventoryService inventoryService;
    @Mock DonorService donorService;
    @Mock NotificationService notifications;

    @InjectMocks DonationService service;

    @Test
    void schedule_creates_scheduled_donation() {
        var donorUser = new User();
        donorUser.setId(10L);
        donorUser.setRole(Role.DONOR);

        var donor = Donor.builder()
                .userId(10L)
                .user(donorUser)
                .bloodType(BloodType.O_POS)
                .eligible(true)
                .active(true)
                .build();

        var bank = BloodBank.builder().id(2L).active(true).build();
        when(donors.findByUserId(10L)).thenReturn(Optional.of(donor));
        when(banks.findById(2L)).thenReturn(Optional.of(bank));
        when(donations.save(any(Donation.class))).thenAnswer(inv -> inv.getArgument(0));

        var dto = new ScheduleDonationDto(2L, null, Instant.now().plus(2, ChronoUnit.DAYS), null);
        var saved = service.schedule(donorUser, dto);

        assertThat(saved.getDonor().getId()).isEqualTo(10L);
        assertThat(saved.getBloodBank().getId()).isEqualTo(2L);
        assertThat(saved.getStatus()).isEqualTo(DonationStatus.SCHEDULED);
        assertThat(saved.getBloodType()).isEqualTo(BloodType.O_POS);
    }

    @Test
    void complete_updates_donor_inventory_and_notifies() {
        var donorUser = new User();
        donorUser.setId(10L);
        donorUser.setRole(Role.DONOR);

        var bank = BloodBank.builder().id(2L).active(true).name("Bank").build();
        var donation = Donation.builder()
                .id(1L)
                .donor(donorUser)
                .bloodBank(bank)
                .bloodType(BloodType.O_POS)
                .status(DonationStatus.CONFIRMED)
                .appointmentDate(Instant.now().plus(1, ChronoUnit.DAYS))
                .build();

        var donor = Donor.builder()
                .userId(10L)
                .user(donorUser)
                .bloodType(BloodType.O_POS)
                .eligible(true)
                .active(true)
                .build();

        when(donations.findById(1L)).thenReturn(Optional.of(donation));
        when(donors.findByUserId(10L)).thenReturn(Optional.of(donor));
        when(donors.save(any(Donor.class))).thenAnswer(inv -> inv.getArgument(0));

        var updated = service.complete(1L, 2);

        assertThat(updated.getStatus()).isEqualTo(DonationStatus.COMPLETED);
        assertThat(updated.getUnitsCollected()).isEqualTo(2);
        assertThat(donor.getLastDonationDate()).isNotNull();
        verify(donorService).recalculateEligibility(donor);
        verify(inventoryService).addCollectedUnits(eq(bank), eq(BloodType.O_POS), eq(2));
        verify(notifications).notifyDonationCompleted(donation);
    }
}

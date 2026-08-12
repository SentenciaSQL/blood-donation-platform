package com.afriasdev.dds.service;

import com.afriasdev.dds.api.dto.donation.ScheduleDonationDto;
import com.afriasdev.dds.domain.BloodBank;
import com.afriasdev.dds.domain.Donation;
import com.afriasdev.dds.domain.DonationStatus;
import com.afriasdev.dds.domain.Donor;
import com.afriasdev.dds.domain.Request;
import com.afriasdev.dds.domain.RequestStatus;
import com.afriasdev.dds.domain.Role;
import com.afriasdev.dds.domain.User;
import com.afriasdev.dds.exception.BadRequestException;
import com.afriasdev.dds.exception.ForbiddenException;
import com.afriasdev.dds.exception.ResourceNotFoundException;
import com.afriasdev.dds.repository.BloodBankRepository;
import com.afriasdev.dds.repository.DonationRepository;
import com.afriasdev.dds.repository.DonorRepository;
import com.afriasdev.dds.repository.RequestRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Service
public class DonationService {
    private static final Set<DonationStatus> CANCELLABLE = EnumSet.of(
            DonationStatus.SCHEDULED, DonationStatus.CONFIRMED
    );

    private final DonationRepository donations;
    private final BloodBankRepository banks;
    private final RequestRepository requests;
    private final DonorRepository donors;
    private final InventoryService inventoryService;
    private final DonorService donorService;
    private final NotificationService notifications;

    public DonationService(
            DonationRepository donations,
            BloodBankRepository banks,
            RequestRepository requests,
            DonorRepository donors,
            InventoryService inventoryService,
            DonorService donorService,
            NotificationService notifications
    ) {
        this.donations = donations;
        this.banks = banks;
        this.requests = requests;
        this.donors = donors;
        this.inventoryService = inventoryService;
        this.donorService = donorService;
        this.notifications = notifications;
    }

    @Transactional(readOnly = true)
    public List<Donation> myDonations(User donor) {
        return donations.findByDonorOrderByAppointmentDateDesc(donor);
    }

    @Transactional(readOnly = true)
    public Page<Donation> findAll(Pageable pageable) {
        return donations.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Donation findById(Long id) {
        return donations.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Donation not found"));
    }

    @Transactional(readOnly = true)
    public Donation findByIdForUser(Long id, User user) {
        Donation donation = findById(id);
        if (user.getRole() != Role.ADMIN && !donation.getDonor().getId().equals(user.getId())) {
            throw new ForbiddenException("You cannot access another user's donation");
        }
        return donation;
    }

    @Transactional
    public Donation schedule(User donorUser, ScheduleDonationDto dto) {
        if (donorUser.getRole() != Role.DONOR) {
            throw new ForbiddenException("Only donors can schedule donations");
        }

        Donor donor = donors.findByUserId(donorUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Donor profile not found"));
        donorService.recalculateEligibility(donor);
        if (!Boolean.TRUE.equals(donor.getEligible()) || !Boolean.TRUE.equals(donor.getActive())) {
            throw new BadRequestException("Donor is not currently eligible to donate");
        }

        BloodBank bank = banks.findById(dto.bloodBankId())
                .orElseThrow(() -> new ResourceNotFoundException("Blood bank not found"));
        if (Boolean.FALSE.equals(bank.getActive())) {
            throw new BadRequestException("Blood bank is inactive");
        }

        Request req = null;
        if (dto.requestId() != null) {
            req = requests.findById(dto.requestId())
                    .orElseThrow(() -> new ResourceNotFoundException("Blood request not found"));
        }

        Donation d = Donation.builder()
                .donor(donorUser)
                .bloodBank(bank)
                .request(req)
                .appointmentDate(dto.appointmentDate())
                .status(DonationStatus.SCHEDULED)
                .bloodType(donor.getBloodType())
                .notes(dto.notes())
                .createdAt(Instant.now())
                .build();
        return donations.save(d);
    }

    @Transactional
    public Donation cancel(Long id, User user) {
        Donation donation = findByIdForUser(id, user);
        if (!CANCELLABLE.contains(donation.getStatus())) {
            throw new BadRequestException("Donation cannot be cancelled in status " + donation.getStatus());
        }
        donation.setStatus(DonationStatus.CANCELLED);
        donation.setUpdatedAt(Instant.now());
        notifications.notifyDonationCancelled(donation);
        return donation;
    }

    @Transactional
    public Donation confirm(Long id) {
        Donation donation = findById(id);
        if (donation.getStatus() != DonationStatus.SCHEDULED) {
            throw new BadRequestException("Only scheduled donations can be confirmed");
        }
        donation.setStatus(DonationStatus.CONFIRMED);
        donation.setUpdatedAt(Instant.now());
        notifications.notifyDonationConfirmed(donation);
        return donation;
    }

    @Transactional
    public Donation complete(Long id, Integer unitsCollected) {
        Donation donation = findById(id);
        if (donation.getStatus() != DonationStatus.SCHEDULED && donation.getStatus() != DonationStatus.CONFIRMED) {
            throw new BadRequestException("Only scheduled/confirmed donations can be completed");
        }

        int units = unitsCollected == null || unitsCollected < 1 ? 1 : unitsCollected;
        donation.setStatus(DonationStatus.COMPLETED);
        donation.setUnitsCollected(units);
        donation.setUpdatedAt(Instant.now());

        Donor donor = donors.findByUserId(donation.getDonor().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Donor profile not found"));
        donor.setLastDonationDate(Instant.now());
        donorService.recalculateEligibility(donor);
        donors.save(donor);

        inventoryService.addCollectedUnits(donation.getBloodBank(), donation.getBloodType(), units);

        if (donation.getRequest() != null
                && donation.getRequest().getStatus() != RequestStatus.FULFILLED
                && donation.getRequest().getStatus() != RequestStatus.CANCELLED) {
            donation.getRequest().setStatus(RequestStatus.FULFILLED);
            donation.getRequest().setUpdatedAt(Instant.now());
            notifications.notifyRequestStatusChanged(donation.getRequest());
        }

        notifications.notifyDonationCompleted(donation);
        return donation;
    }

    @Transactional
    public Donation markNoShow(Long id) {
        Donation donation = findById(id);
        if (donation.getStatus() != DonationStatus.SCHEDULED && donation.getStatus() != DonationStatus.CONFIRMED) {
            throw new BadRequestException("Only scheduled/confirmed donations can be marked as no-show");
        }
        donation.setStatus(DonationStatus.NO_SHOW);
        donation.setUpdatedAt(Instant.now());
        return donation;
    }
}

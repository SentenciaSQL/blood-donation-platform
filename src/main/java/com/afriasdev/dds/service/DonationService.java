package com.afriasdev.dds.service;

import com.afriasdev.dds.api.dto.donation.ScheduleDonationDto;
import com.afriasdev.dds.api.dto.donation.UpdateDonationStatusDto;
import com.afriasdev.dds.domain.BloodBank;
import com.afriasdev.dds.domain.Donation;
import com.afriasdev.dds.domain.Request;
import com.afriasdev.dds.domain.User;
import com.afriasdev.dds.exception.BadRequestException;
import com.afriasdev.dds.exception.ResourceNotFoundException;
import com.afriasdev.dds.repository.BloodBankRepository;
import com.afriasdev.dds.repository.DonationRepository;
import com.afriasdev.dds.repository.RequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Set;

@Service
public class DonationService {
    private static final Set<String> ALLOWED_STATUSES = Set.of("SCHEDULED", "COMPLETED", "CANCELLED");

    private final DonationRepository donations;
    private final BloodBankRepository banks;
    private final RequestRepository requests;

    public DonationService(DonationRepository donations, BloodBankRepository banks, RequestRepository requests) {
        this.donations = donations;
        this.banks = banks;
        this.requests = requests;
    }

    @Transactional(readOnly = true)
    public List<Donation> myDonations(User donor) {
        return donations.findAll().stream()
                .filter(d -> d.getDonor() != null && d.getDonor().getId().equals(donor.getId()))
                .toList();
    }

    @Transactional
    public Donation schedule(User donor, ScheduleDonationDto dto) {
        BloodBank bank = banks.findById(dto.bloodBankId())
                .orElseThrow(() -> new ResourceNotFoundException("Blood bank not found"));

        Request req = null;
        if (dto.requestId() != null) {
            req = requests.findById(dto.requestId())
                    .orElseThrow(() -> new ResourceNotFoundException("Blood request not found"));
        }

        Donation d = Donation.builder()
                .donor(donor)
                .bloodBank(bank)
                .request(req)
                .scheduledAt(dto.scheduledAt())
                .status("SCHEDULED")
                .createdAt(Instant.now())
                .build();
        return donations.save(d);
    }

    @Transactional(readOnly = true)
    public Donation findById(Long id) {
        return donations.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Donation not found"));
    }

    @Transactional
    public Donation updateStatus(Long id, UpdateDonationStatusDto dto) {
        if (!ALLOWED_STATUSES.contains(dto.status())) {
            throw new BadRequestException("Invalid donation status. Allowed: SCHEDULED, COMPLETED, CANCELLED");
        }
        Donation d = findById(id);
        d.setStatus(dto.status());
        return d;
    }
}

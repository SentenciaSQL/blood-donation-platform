package com.afriasdev.dds.service;

import com.afriasdev.dds.api.dto.donation.ScheduleDonationDto;
import com.afriasdev.dds.api.dto.donation.UpdateDonationStatusDto;
import com.afriasdev.dds.domain.BloodBank;
import com.afriasdev.dds.domain.Donation;
import com.afriasdev.dds.domain.Request;
import com.afriasdev.dds.domain.User;
import com.afriasdev.dds.repository.BloodBankRepository;
import com.afriasdev.dds.repository.DonationRepository;
import com.afriasdev.dds.repository.RequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class DonationService {
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
                .filter(d -> d.getDonor().getId().equals(donor.getId()))
                .toList();
    }

    @Transactional
    public Donation schedule(User donor, ScheduleDonationDto dto) {
        BloodBank bank = banks.findById(dto.bloodBankId()).orElseThrow();
        Request req = dto.requestId() == null ? null : requests.findById(dto.requestId()).orElse(null);

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

    @Transactional
    public Donation findById(Long id) {
        return donations.findById(id).orElseThrow();
    }

    @Transactional
    public Donation updateStatus(Long id, UpdateDonationStatusDto dto) {
        Donation d = findById(id);
        d.setStatus(dto.status());
        return d;
    }

}

package com.afriasdev.dds.service;

import com.afriasdev.dds.api.dto.bank.CreateBloodBankDto;
import com.afriasdev.dds.domain.BloodBank;
import com.afriasdev.dds.repository.BloodBankRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class BloodBankService {
    private final BloodBankRepository banks;

    public BloodBankService(BloodBankRepository banks) {
        this.banks = banks;
    }

    @Transactional
    public BloodBank create(CreateBloodBankDto dto) {
        var bank = BloodBank.builder()
                .name(dto.name())
                .address(dto.address())
                .phone(dto.phone())
                .latitude(dto.latitude())
                .longitude(dto.longitude())
                .createdAt(Instant.now())
                .build();

        return banks.save(bank);
    }

    @Transactional(readOnly = true)
    public List<BloodBank> findAll() {
        return banks.findAll();
    }

    @Transactional(readOnly = true)
    public BloodBank findById(Long id) {
        return banks.findById(id).orElseThrow();
    }

    @Transactional
    public void delete(Long id) {
        banks.deleteById(id);
    }
}

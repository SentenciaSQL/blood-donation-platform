package com.afriasdev.dds.service;

import com.afriasdev.dds.api.dto.bank.CreateBloodBankDto;
import com.afriasdev.dds.api.dto.bank.UpdateBloodBankDto;
import com.afriasdev.dds.domain.BloodBank;
import com.afriasdev.dds.exception.ResourceNotFoundException;
import com.afriasdev.dds.repository.BloodBankRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
                .city(dto.city())
                .phone(dto.phone())
                .email(dto.email())
                .latitude(dto.latitude())
                .longitude(dto.longitude())
                .active(dto.active() == null || dto.active())
                .createdAt(Instant.now())
                .build();
        return banks.save(bank);
    }

    @Transactional
    public BloodBank update(Long id, UpdateBloodBankDto dto) {
        BloodBank bank = findById(id);
        bank.setName(dto.name());
        bank.setAddress(dto.address());
        bank.setCity(dto.city());
        bank.setPhone(dto.phone());
        bank.setEmail(dto.email());
        bank.setLatitude(dto.latitude());
        bank.setLongitude(dto.longitude());
        if (dto.active() != null) {
            bank.setActive(dto.active());
        }
        bank.setUpdatedAt(Instant.now());
        return banks.save(bank);
    }

    @Transactional(readOnly = true)
    public List<BloodBank> findAll() {
        return banks.findAll();
    }

    @Transactional(readOnly = true)
    public Page<BloodBank> search(String city, Boolean active, Pageable pageable) {
        return banks.search(city, active, pageable);
    }

    @Transactional(readOnly = true)
    public BloodBank findById(Long id) {
        return banks.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Blood bank not found"));
    }

    @Transactional
    public void delete(Long id) {
        if (!banks.existsById(id)) {
            throw new ResourceNotFoundException("Blood bank not found");
        }
        banks.deleteById(id);
    }
}

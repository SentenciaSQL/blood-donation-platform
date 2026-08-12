package com.afriasdev.dds.service;

import com.afriasdev.dds.api.dto.inventory.UpdateInventoryDto;
import com.afriasdev.dds.domain.BloodBank;
import com.afriasdev.dds.domain.Inventory;
import com.afriasdev.dds.exception.BadRequestException;
import com.afriasdev.dds.exception.ResourceNotFoundException;
import com.afriasdev.dds.repository.BloodBankRepository;
import com.afriasdev.dds.repository.InventoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class InventoryService {
    private final InventoryRepository invs;
    private final BloodBankRepository banks;

    public InventoryService(InventoryRepository invs, BloodBankRepository banks) {
        this.invs = invs;
        this.banks = banks;
    }

    @Transactional(readOnly = true)
    public List<Inventory> findByBank(Long bankId) {
        BloodBank bank = banks.findById(bankId)
                .orElseThrow(() -> new ResourceNotFoundException("Blood bank not found"));
        List<Inventory> inventories = bank.getInventories();
        return inventories == null ? List.of() : List.copyOf(inventories);
    }

    @Transactional
    public Inventory upsert(Long bankId, UpdateInventoryDto dto) {
        BloodBank bank = banks.findById(bankId)
                .orElseThrow(() -> new ResourceNotFoundException("Blood bank not found"));

        if (bank.getInventories() == null) {
            bank.setInventories(new ArrayList<>());
        }

        Inventory inv = bank.getInventories().stream()
                .filter(x -> x.getBloodType().equals(dto.bloodType()))
                .findFirst()
                .orElse(null);

        int units = dto.unitsAvailable() == null ? 0 : dto.unitsAvailable();
        if (units < 0) {
            throw new BadRequestException("Inventory units cannot be negative");
        }

        if (inv == null) {
            inv = Inventory.builder()
                    .bloodBank(bank)
                    .bloodType(dto.bloodType())
                    .unitsAvailable(units)
                    .updatedAt(Instant.now())
                    .build();
            bank.getInventories().add(inv);
        } else {
            inv.setUnitsAvailable(units);
            inv.setUpdatedAt(Instant.now());
        }
        return invs.save(inv);
    }
}

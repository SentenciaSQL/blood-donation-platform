package com.afriasdev.dds.service;

import com.afriasdev.dds.api.dto.inventory.UpdateInventoryDto;
import com.afriasdev.dds.domain.BloodBank;
import com.afriasdev.dds.domain.Inventory;
import com.afriasdev.dds.repository.BloodBankRepository;
import com.afriasdev.dds.repository.InventoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
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
        BloodBank bank = banks.findById(bankId).orElseThrow();
        return bank.getInventories();
    }

    @Transactional
    public Inventory upsert(Long bankId, UpdateInventoryDto dto) {
        BloodBank bank = banks.findById(bankId).orElseThrow();

        var list = bank.getInventories();
        Inventory inv = list.stream()
                .filter(x -> x.getBloodType().equals(dto.bloodType()))
                .findFirst().orElse(null);

        if (inv == null) {
            inv = Inventory.builder()
                    .bloodBank(bank)
                    .bloodType(dto.bloodType())
                    .unitsAvailable(dto.unitsAvailable() == null ? 0 : dto.unitsAvailable())
                    .updatedAt(Instant.now())
                    .build();
        } else {
            inv.setUnitsAvailable(dto.unitsAvailable());
            inv.setUpdatedAt(Instant.now());
        }
        return invs.save(inv);
    }
}

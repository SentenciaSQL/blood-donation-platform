package com.afriasdev.dds.service;

import com.afriasdev.dds.api.dto.inventory.CreateInventoryDto;
import com.afriasdev.dds.api.dto.inventory.UpdateInventoryDto;
import com.afriasdev.dds.api.dto.inventory.UpdateInventoryItemDto;
import com.afriasdev.dds.domain.BloodBank;
import com.afriasdev.dds.domain.BloodType;
import com.afriasdev.dds.domain.Inventory;
import com.afriasdev.dds.exception.BadRequestException;
import com.afriasdev.dds.exception.ConflictException;
import com.afriasdev.dds.exception.ResourceNotFoundException;
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
    private final NotificationService notifications;

    public InventoryService(
            InventoryRepository invs,
            BloodBankRepository banks,
            NotificationService notifications
    ) {
        this.invs = invs;
        this.banks = banks;
        this.notifications = notifications;
    }

    @Transactional(readOnly = true)
    public List<Inventory> findAll() {
        return invs.findAll();
    }

    @Transactional(readOnly = true)
    public List<Inventory> findByBank(Long bankId) {
        if (!banks.existsById(bankId)) {
            throw new ResourceNotFoundException("Blood bank not found");
        }
        return invs.findByBloodBankId(bankId);
    }

    @Transactional(readOnly = true)
    public Inventory findById(Long id) {
        return invs.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory item not found"));
    }

    @Transactional(readOnly = true)
    public List<Inventory> findLowStock() {
        return invs.findLowStock();
    }

    @Transactional
    public Inventory create(CreateInventoryDto dto) {
        BloodBank bank = banks.findById(dto.bloodBankId())
                .orElseThrow(() -> new ResourceNotFoundException("Blood bank not found"));
        BloodType bloodType = BloodType.fromCode(dto.bloodType());

        invs.findByBloodBankIdAndBloodType(bank.getId(), bloodType).ifPresent(existing -> {
            throw new ConflictException("Inventory already exists for this blood bank and blood type");
        });

        Inventory inv = Inventory.builder()
                .bloodBank(bank)
                .bloodType(bloodType)
                .unitsAvailable(dto.unitsAvailable())
                .minimumStock(dto.minimumStock() == null ? 5 : dto.minimumStock())
                .updatedAt(Instant.now())
                .build();
        Inventory saved = invs.save(inv);
        notifyIfLowStock(saved);
        return saved;
    }

    @Transactional
    public Inventory update(Long id, UpdateInventoryItemDto dto) {
        Inventory inv = findById(id);
        if (dto.unitsAvailable() != null) {
            inv.setUnitsAvailable(dto.unitsAvailable());
        }
        if (dto.minimumStock() != null) {
            inv.setMinimumStock(dto.minimumStock());
        }
        inv.setUpdatedAt(Instant.now());
        Inventory saved = invs.save(inv);
        notifyIfLowStock(saved);
        return saved;
    }

    @Transactional
    public Inventory addUnits(Long id, int units) {
        if (units <= 0) {
            throw new BadRequestException("Units to add must be positive");
        }
        Inventory inv = findById(id);
        inv.setUnitsAvailable(inv.getUnitsAvailable() + units);
        inv.setUpdatedAt(Instant.now());
        return invs.save(inv);
    }

    @Transactional
    public Inventory removeUnits(Long id, int units) {
        if (units <= 0) {
            throw new BadRequestException("Units to remove must be positive");
        }
        Inventory inv = findById(id);
        int available = inv.getUnitsAvailable() == null ? 0 : inv.getUnitsAvailable();
        if (available < units) {
            throw new BadRequestException("Insufficient inventory units");
        }
        inv.setUnitsAvailable(available - units);
        inv.setUpdatedAt(Instant.now());
        Inventory saved = invs.save(inv);
        notifyIfLowStock(saved);
        return saved;
    }

    /**
     * Backward-compatible upsert used by nested bank inventory endpoint.
     */
    @Transactional
    public Inventory upsert(Long bankId, UpdateInventoryDto dto) {
        BloodBank bank = banks.findById(bankId)
                .orElseThrow(() -> new ResourceNotFoundException("Blood bank not found"));
        BloodType bloodType = BloodType.fromCode(dto.bloodType());

        Inventory inv = invs.findByBloodBankIdAndBloodType(bankId, bloodType).orElse(null);
        int units = dto.unitsAvailable() == null ? 0 : dto.unitsAvailable();
        if (units < 0) {
            throw new BadRequestException("Inventory units cannot be negative");
        }

        if (inv == null) {
            inv = Inventory.builder()
                    .bloodBank(bank)
                    .bloodType(bloodType)
                    .unitsAvailable(units)
                    .minimumStock(5)
                    .updatedAt(Instant.now())
                    .build();
        } else {
            inv.setUnitsAvailable(units);
            inv.setUpdatedAt(Instant.now());
        }
        Inventory saved = invs.save(inv);
        notifyIfLowStock(saved);
        return saved;
    }

    @Transactional
    public void addCollectedUnits(BloodBank bank, BloodType bloodType, int units) {
        if (units <= 0) {
            return;
        }
        Inventory inv = invs.findByBloodBankIdAndBloodType(bank.getId(), bloodType)
                .orElseGet(() -> Inventory.builder()
                        .bloodBank(bank)
                        .bloodType(bloodType)
                        .unitsAvailable(0)
                        .minimumStock(5)
                        .updatedAt(Instant.now())
                        .build());
        inv.setUnitsAvailable((inv.getUnitsAvailable() == null ? 0 : inv.getUnitsAvailable()) + units);
        inv.setUpdatedAt(Instant.now());
        invs.save(inv);
    }

    private void notifyIfLowStock(Inventory inv) {
        int units = inv.getUnitsAvailable() == null ? 0 : inv.getUnitsAvailable();
        int min = inv.getMinimumStock() == null ? 0 : inv.getMinimumStock();
        if (units <= min) {
            notifications.notifyAdminsLowStock(inv);
        }
    }
}

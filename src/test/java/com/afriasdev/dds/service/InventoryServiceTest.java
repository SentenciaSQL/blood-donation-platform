package com.afriasdev.dds.service;

import com.afriasdev.dds.api.dto.inventory.UpdateInventoryDto;
import com.afriasdev.dds.domain.BloodBank;
import com.afriasdev.dds.domain.Inventory;
import com.afriasdev.dds.repository.BloodBankRepository;
import com.afriasdev.dds.repository.InventoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class InventoryServiceTest {
    @Mock
    InventoryRepository invs;

    @Mock
    BloodBankRepository banks;

    @InjectMocks
    InventoryService service;

    @Test
    void upsert_creates_when_not_exists() {
        var bank = BloodBank.builder().id(5L).inventories(new ArrayList<>()).build();
        when(banks.findById(5L)).thenReturn(Optional.of(bank));
        when(invs.save(any(Inventory.class))).thenAnswer(inv -> inv.getArgument(0));

        var dto = new UpdateInventoryDto("O+", 12);
        var result = service.upsert(5L, dto);

        assertThat(result.getBloodType()).isEqualTo("O+");
        assertThat(result.getUnitsAvailable()).isEqualTo(12);
        assertThat(result.getBloodBank()).isSameAs(bank);
    }

    @Test
    void upsert_updates_when_exists() {
        var existing = Inventory.builder().bloodType("A+").unitsAvailable(3).build();
        var bank = BloodBank.builder().id(5L).inventories(new ArrayList<>(List.of(existing))).build();
        when(banks.findById(5L)).thenReturn(Optional.of(bank));
        when(invs.save(any(Inventory.class))).thenAnswer(inv -> inv.getArgument(0));

        var dto = new UpdateInventoryDto("A+", 20);
        var result = service.upsert(5L, dto);

        assertThat(result).isSameAs(existing);
        assertThat(existing.getUnitsAvailable()).isEqualTo(20);
    }
}

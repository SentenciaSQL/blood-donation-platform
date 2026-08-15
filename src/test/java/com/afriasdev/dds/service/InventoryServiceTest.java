package com.afriasdev.dds.service;

import com.afriasdev.dds.api.dto.inventory.UpdateInventoryDto;
import com.afriasdev.dds.domain.BloodBank;
import com.afriasdev.dds.domain.BloodType;
import com.afriasdev.dds.domain.Inventory;
import com.afriasdev.dds.exception.BadRequestException;
import com.afriasdev.dds.repository.BloodBankRepository;
import com.afriasdev.dds.repository.InventoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock InventoryRepository invs;
    @Mock BloodBankRepository banks;
    @Mock NotificationService notifications;

    @InjectMocks InventoryService service;

    @Test
    void upsert_creates_when_not_exists() {
        var bank = BloodBank.builder().id(5L).name("Bank").build();
        when(banks.findById(5L)).thenReturn(Optional.of(bank));
        when(invs.findByBloodBankIdAndBloodType(5L, BloodType.O_POS)).thenReturn(Optional.empty());
        when(invs.save(any(Inventory.class))).thenAnswer(inv -> inv.getArgument(0));

        var result = service.upsert(5L, new UpdateInventoryDto("O+", 12));

        assertThat(result.getBloodType()).isEqualTo(BloodType.O_POS);
        assertThat(result.getUnitsAvailable()).isEqualTo(12);
        assertThat(result.getBloodBank()).isSameAs(bank);
    }

    @Test
    void upsert_updates_when_exists() {
        var bank = BloodBank.builder().id(5L).name("Bank").build();
        var existing = Inventory.builder()
                .id(1L)
                .bloodBank(bank)
                .bloodType(BloodType.A_POS)
                .unitsAvailable(3)
                .minimumStock(5)
                .build();
        when(banks.findById(5L)).thenReturn(Optional.of(bank));
        when(invs.findByBloodBankIdAndBloodType(5L, BloodType.A_POS)).thenReturn(Optional.of(existing));
        when(invs.save(any(Inventory.class))).thenAnswer(inv -> inv.getArgument(0));

        var result = service.upsert(5L, new UpdateInventoryDto("A+", 20));

        assertThat(result).isSameAs(existing);
        assertThat(existing.getUnitsAvailable()).isEqualTo(20);
    }

    @Test
    void removeUnits_prevents_negative_stock() {
        var inv = Inventory.builder()
                .id(9L)
                .unitsAvailable(2)
                .minimumStock(5)
                .bloodType(BloodType.O_POS)
                .bloodBank(BloodBank.builder().id(1L).name("B").build())
                .build();
        when(invs.findById(9L)).thenReturn(Optional.of(inv));

        assertThatThrownBy(() -> service.removeUnits(9L, 3))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Insufficient");
    }
}

package com.afriasdev.dds.repository;

import com.afriasdev.dds.domain.BloodType;
import com.afriasdev.dds.domain.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    List<Inventory> findByBloodBankId(Long bloodBankId);

    Optional<Inventory> findByBloodBankIdAndBloodType(Long bloodBankId, BloodType bloodType);

    @Query("SELECT i FROM Inventory i WHERE i.unitsAvailable <= i.minimumStock")
    List<Inventory> findLowStock();
}

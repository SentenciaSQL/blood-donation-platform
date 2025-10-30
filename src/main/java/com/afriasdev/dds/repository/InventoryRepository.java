package com.afriasdev.dds.repository;

import com.afriasdev.dds.domain.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {
}

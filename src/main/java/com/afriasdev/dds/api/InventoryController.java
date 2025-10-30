package com.afriasdev.dds.api;

import com.afriasdev.dds.api.dto.inventory.UpdateInventoryDto;
import com.afriasdev.dds.domain.Inventory;
import com.afriasdev.dds.service.InventoryService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/blood-banks/{bankId}/inventory")
public class InventoryController {

    private final InventoryService service;

    public InventoryController(InventoryService service) {
        this.service = service;
    }

    @GetMapping
    public List<Inventory> list(@PathVariable Long bankId) {
        return service.findByBank(bankId);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping
    public Inventory upsert(@PathVariable Long bankId, @Valid @RequestBody UpdateInventoryDto dto) {
        return service.upsert(bankId, dto);
    }
}

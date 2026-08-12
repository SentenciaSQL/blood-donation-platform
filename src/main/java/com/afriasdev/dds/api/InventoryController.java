package com.afriasdev.dds.api;

import com.afriasdev.dds.api.dto.inventory.InventoryResponseDto;
import com.afriasdev.dds.api.dto.inventory.UpdateInventoryDto;
import com.afriasdev.dds.api.mapper.EntityMapper;
import com.afriasdev.dds.service.InventoryService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Nested inventory endpoints kept for backward compatibility.
 */
@RestController
@RequestMapping("/blood-banks/{bankId}/inventory")
public class InventoryController {

    private final InventoryService service;
    private final EntityMapper mapper;

    public InventoryController(InventoryService service, EntityMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @GetMapping
    public List<InventoryResponseDto> list(@PathVariable Long bankId) {
        return service.findByBank(bankId).stream()
                .map(mapper::toInventoryResponse)
                .toList();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping
    public InventoryResponseDto upsert(
            @PathVariable Long bankId,
            @Valid @RequestBody UpdateInventoryDto dto
    ) {
        return mapper.toInventoryResponse(service.upsert(bankId, dto));
    }
}

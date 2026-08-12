package com.afriasdev.dds.api;

import com.afriasdev.dds.api.dto.inventory.CreateInventoryDto;
import com.afriasdev.dds.api.dto.inventory.InventoryResponseDto;
import com.afriasdev.dds.api.dto.inventory.StockOperationDto;
import com.afriasdev.dds.api.dto.inventory.UpdateInventoryItemDto;
import com.afriasdev.dds.api.mapper.EntityMapper;
import com.afriasdev.dds.service.InventoryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/inventory")
@PreAuthorize("hasRole('ADMIN')")
public class InventoryAdminController {

    private final InventoryService service;
    private final EntityMapper mapper;

    public InventoryAdminController(InventoryService service, EntityMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @GetMapping
    public List<InventoryResponseDto> all() {
        return service.findAll().stream().map(mapper::toInventoryResponse).toList();
    }

    @GetMapping("/blood-bank/{bloodBankId}")
    public List<InventoryResponseDto> byBank(@PathVariable Long bloodBankId) {
        return service.findByBank(bloodBankId).stream().map(mapper::toInventoryResponse).toList();
    }

    @GetMapping("/low-stock")
    public List<InventoryResponseDto> lowStock() {
        return service.findLowStock().stream().map(mapper::toInventoryResponse).toList();
    }

    @PostMapping
    public ResponseEntity<InventoryResponseDto> create(@Valid @RequestBody CreateInventoryDto dto) {
        var saved = service.create(dto);
        return ResponseEntity
                .created(URI.create("/inventory/" + saved.getId()))
                .body(mapper.toInventoryResponse(saved));
    }

    @PutMapping("/{id}")
    public InventoryResponseDto update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateInventoryItemDto dto
    ) {
        return mapper.toInventoryResponse(service.update(id, dto));
    }

    @PostMapping("/{id}/add")
    public InventoryResponseDto add(
            @PathVariable Long id,
            @Valid @RequestBody StockOperationDto dto
    ) {
        return mapper.toInventoryResponse(service.addUnits(id, dto.units()));
    }

    @PostMapping("/{id}/remove")
    public InventoryResponseDto remove(
            @PathVariable Long id,
            @Valid @RequestBody StockOperationDto dto
    ) {
        return mapper.toInventoryResponse(service.removeUnits(id, dto.units()));
    }
}

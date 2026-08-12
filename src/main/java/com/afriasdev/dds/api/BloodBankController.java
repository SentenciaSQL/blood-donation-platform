package com.afriasdev.dds.api;

import com.afriasdev.dds.api.dto.bank.BloodBankResponseDto;
import com.afriasdev.dds.api.dto.bank.CreateBloodBankDto;
import com.afriasdev.dds.api.mapper.EntityMapper;
import com.afriasdev.dds.service.BloodBankService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/blood-banks")
public class BloodBankController {

    private final BloodBankService service;
    private final EntityMapper mapper;

    public BloodBankController(BloodBankService service, EntityMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<BloodBankResponseDto> create(@Valid @RequestBody CreateBloodBankDto dto) {
        var saved = service.create(dto);
        return ResponseEntity
                .created(URI.create("/blood-banks/" + saved.getId()))
                .body(mapper.toBloodBankResponse(saved));
    }

    @GetMapping
    public List<BloodBankResponseDto> all() {
        return service.findAll().stream()
                .map(mapper::toBloodBankResponse)
                .toList();
    }

    @GetMapping("/{id}")
    public BloodBankResponseDto get(@PathVariable Long id) {
        return mapper.toBloodBankResponse(service.findById(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}

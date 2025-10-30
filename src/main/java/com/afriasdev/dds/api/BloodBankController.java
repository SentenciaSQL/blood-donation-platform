package com.afriasdev.dds.api;

import com.afriasdev.dds.api.dto.bank.CreateBloodBankDto;
import com.afriasdev.dds.domain.BloodBank;
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

    public BloodBankController(BloodBankService service) {
        this.service = service;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<BloodBank> create(@Valid @RequestBody CreateBloodBankDto dto) {
        var saved = service.create(dto);
        return ResponseEntity.created(URI.create("/blood-banks/" + saved.getId())).body(saved);
    }

    @GetMapping
    public List<BloodBank> all() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public BloodBank get(@PathVariable Long id) {
        return service.findById(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}

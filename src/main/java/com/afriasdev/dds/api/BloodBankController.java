package com.afriasdev.dds.api;

import com.afriasdev.dds.api.dto.bank.BloodBankResponseDto;
import com.afriasdev.dds.api.dto.bank.CreateBloodBankDto;
import com.afriasdev.dds.api.dto.bank.UpdateBloodBankDto;
import com.afriasdev.dds.api.dto.common.PageResponse;
import com.afriasdev.dds.api.mapper.EntityMapper;
import com.afriasdev.dds.service.BloodBankService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/blood-banks")
public class BloodBankController {

    private final BloodBankService service;
    private final EntityMapper mapper;

    public BloodBankController(BloodBankService service, EntityMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @GetMapping
    public PageResponse<BloodBankResponseDto> search(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) Boolean active,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return PageResponse.from(service.search(city, active, pageable).map(mapper::toBloodBankResponse));
    }

    @GetMapping("/{id}")
    public BloodBankResponseDto get(@PathVariable Long id) {
        return mapper.toBloodBankResponse(service.findById(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<BloodBankResponseDto> create(@Valid @RequestBody CreateBloodBankDto dto) {
        var saved = service.create(dto);
        return ResponseEntity
                .created(URI.create("/blood-banks/" + saved.getId()))
                .body(mapper.toBloodBankResponse(saved));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public BloodBankResponseDto update(@PathVariable Long id, @Valid @RequestBody UpdateBloodBankDto dto) {
        return mapper.toBloodBankResponse(service.update(id, dto));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}

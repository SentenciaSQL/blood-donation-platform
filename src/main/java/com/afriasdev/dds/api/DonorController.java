package com.afriasdev.dds.api;

import com.afriasdev.dds.api.dto.donor.DonorResponseDto;
import com.afriasdev.dds.api.mapper.EntityMapper;
import com.afriasdev.dds.service.DonorService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/donors")
public class DonorController {

    private final DonorService donorService;
    private final EntityMapper mapper;

    public DonorController(DonorService donorService, EntityMapper mapper) {
        this.donorService = donorService;
        this.mapper = mapper;
    }

    @GetMapping
    public List<DonorResponseDto> search(@RequestParam String bloodType) {
        return donorService.searchAvailableByBloodType(bloodType).stream()
                .map(mapper::toDonorResponse)
                .toList();
    }
}

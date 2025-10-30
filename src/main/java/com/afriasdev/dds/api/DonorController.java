package com.afriasdev.dds.api;

import com.afriasdev.dds.domain.Donor;
import com.afriasdev.dds.repository.DonorRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/donors")
public class DonorController {

    private final DonorRepository donors;
    public DonorController(DonorRepository donors) {
        this.donors = donors;
    }

    @GetMapping
    public List<Donor> search(@RequestParam String bloodType) {
        return donors.findByBloodTypeAndAvailabilityTrue(bloodType);
    }
}

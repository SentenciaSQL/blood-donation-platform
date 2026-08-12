package com.afriasdev.dds.service;

import com.afriasdev.dds.domain.Donor;
import com.afriasdev.dds.exception.BadRequestException;
import com.afriasdev.dds.repository.DonorRepository;
import com.afriasdev.dds.util.BloodTypeValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DonorService {

    private final DonorRepository donors;

    public DonorService(DonorRepository donors) {
        this.donors = donors;
    }

    @Transactional(readOnly = true)
    public List<Donor> searchAvailableByBloodType(String bloodType) {
        if (!BloodTypeValidator.isValid(bloodType)) {
            throw new BadRequestException("Invalid blood type");
        }
        return donors.findByBloodTypeAndAvailabilityTrue(bloodType);
    }
}

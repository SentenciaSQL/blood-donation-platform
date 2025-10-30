package com.afriasdev.dds.repository;

import com.afriasdev.dds.domain.Donor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DonorRepository extends JpaRepository<Donor, Long> {
    List<Donor> findByBloodTypeAndAvailabilityTrue(String bloodType);
}

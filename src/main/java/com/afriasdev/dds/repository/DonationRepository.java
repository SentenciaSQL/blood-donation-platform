package com.afriasdev.dds.repository;

import com.afriasdev.dds.domain.Donation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DonationRepository extends JpaRepository<Donation, Long> {
}

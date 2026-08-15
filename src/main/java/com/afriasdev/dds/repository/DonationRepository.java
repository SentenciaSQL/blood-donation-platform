package com.afriasdev.dds.repository;

import com.afriasdev.dds.domain.Donation;
import com.afriasdev.dds.domain.DonationStatus;
import com.afriasdev.dds.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DonationRepository extends JpaRepository<Donation, Long> {

    List<Donation> findByDonorOrderByAppointmentDateDesc(User donor);

    Page<Donation> findByDonor(User donor, Pageable pageable);

    Page<Donation> findByStatus(DonationStatus status, Pageable pageable);

    Page<Donation> findAll(Pageable pageable);

    long countByDonorAndStatus(User donor, DonationStatus status);
}

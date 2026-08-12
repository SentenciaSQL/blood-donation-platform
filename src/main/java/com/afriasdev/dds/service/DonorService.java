package com.afriasdev.dds.service;

import com.afriasdev.dds.api.dto.donor.DonorEligibilityDto;
import com.afriasdev.dds.api.dto.donor.UpdateDonorProfileDto;
import com.afriasdev.dds.domain.BloodType;
import com.afriasdev.dds.domain.Donation;
import com.afriasdev.dds.domain.Donor;
import com.afriasdev.dds.domain.Role;
import com.afriasdev.dds.domain.User;
import com.afriasdev.dds.exception.BadRequestException;
import com.afriasdev.dds.exception.ForbiddenException;
import com.afriasdev.dds.exception.ResourceNotFoundException;
import com.afriasdev.dds.repository.DonationRepository;
import com.afriasdev.dds.repository.DonorRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
public class DonorService {

    public static final int MIN_DONATION_INTERVAL_DAYS = 56;
    public static final BigDecimal MIN_WEIGHT_KG = new BigDecimal("50");
    public static final int MIN_AGE = 18;
    public static final int MAX_AGE = 65;

    private final DonorRepository donors;
    private final DonationRepository donations;

    public DonorService(DonorRepository donors, DonationRepository donations) {
        this.donors = donors;
        this.donations = donations;
    }

    @Transactional(readOnly = true)
    public Donor getMyProfile(User user) {
        requireDonor(user);
        return donors.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Donor profile not found"));
    }

    @Transactional
    public Donor updateMyProfile(User user, UpdateDonorProfileDto dto) {
        Donor donor = getMyProfile(user);

        if (dto.bloodType() != null) {
            donor.setBloodType(BloodType.fromCode(dto.bloodType()));
        }
        if (dto.birthDate() != null) {
            donor.setBirthDate(dto.birthDate());
        }
        if (dto.gender() != null) {
            donor.setGender(dto.gender());
        }
        if (dto.phone() != null) {
            donor.setPhone(dto.phone());
        }
        if (dto.city() != null) {
            donor.setCity(dto.city());
        }
        if (dto.address() != null) {
            donor.setAddress(dto.address());
        }
        if (dto.latitude() != null) {
            donor.setLatitude(dto.latitude());
        }
        if (dto.longitude() != null) {
            donor.setLongitude(dto.longitude());
        }
        if (dto.weight() != null) {
            donor.setWeight(dto.weight());
        }
        if (dto.active() != null) {
            donor.setActive(dto.active());
            donor.setAvailability(dto.active());
        }

        recalculateEligibility(donor);
        return donors.save(donor);
    }

    @Transactional(readOnly = true)
    public DonorEligibilityDto getEligibility(User user) {
        Donor donor = getMyProfile(user);
        List<String> reasons = new ArrayList<>();
        boolean eligible = evaluateEligibility(donor, reasons);
        Instant nextEligible = null;
        if (donor.getLastDonationDate() != null) {
            nextEligible = donor.getLastDonationDate().plus(MIN_DONATION_INTERVAL_DAYS, ChronoUnit.DAYS);
        }
        return new DonorEligibilityDto(eligible, donor.getLastDonationDate(), nextEligible, reasons);
    }

    @Transactional(readOnly = true)
    public List<Donation> getHistory(User user) {
        requireDonor(user);
        return donations.findByDonorOrderByAppointmentDateDesc(user);
    }

    @Transactional(readOnly = true)
    public Page<Donor> search(BloodType bloodType, String city, Boolean eligible, Boolean active, Pageable pageable) {
        return donors.search(bloodType, city, eligible, active, pageable);
    }

    @Transactional(readOnly = true)
    public Donor getById(Long id) {
        return donors.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Donor not found"));
    }

    @Transactional(readOnly = true)
    public List<Donor> searchAvailableByBloodType(String bloodType) {
        if (!com.afriasdev.dds.util.BloodTypeValidator.isValid(bloodType)) {
            throw new BadRequestException("Invalid blood type");
        }
        return donors.findByBloodTypeAndAvailabilityTrue(BloodType.fromCode(bloodType));
    }

    @Transactional
    public void recalculateEligibility(Donor donor) {
        List<String> reasons = new ArrayList<>();
        boolean eligible = evaluateEligibility(donor, reasons);
        donor.setEligible(eligible);
        donor.setAvailability(Boolean.TRUE.equals(donor.getActive()) && eligible);
    }

    public boolean evaluateEligibility(Donor donor, List<String> reasons) {
        if (Boolean.FALSE.equals(donor.getActive())) {
            reasons.add("Donor profile is inactive");
        }
        if (donor.getBirthDate() != null) {
            int age = Period.between(donor.getBirthDate(), LocalDate.now()).getYears();
            if (age < MIN_AGE || age > MAX_AGE) {
                reasons.add("Age must be between " + MIN_AGE + " and " + MAX_AGE);
            }
        }
        if (donor.getWeight() != null && donor.getWeight().compareTo(MIN_WEIGHT_KG) < 0) {
            reasons.add("Weight must be at least " + MIN_WEIGHT_KG + " kg");
        }
        if (donor.getLastDonationDate() != null) {
            Instant next = donor.getLastDonationDate().plus(MIN_DONATION_INTERVAL_DAYS, ChronoUnit.DAYS);
            if (Instant.now().isBefore(next)) {
                reasons.add("Must wait " + MIN_DONATION_INTERVAL_DAYS + " days between donations");
            }
        }
        return reasons.isEmpty() && !Boolean.FALSE.equals(donor.getActive());
    }

    private void requireDonor(User user) {
        if (user.getRole() != Role.DONOR && user.getRole() != Role.ADMIN) {
            throw new ForbiddenException("Only donors can access donor profiles");
        }
    }
}

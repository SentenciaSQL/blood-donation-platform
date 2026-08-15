package com.afriasdev.dds.service;

import com.afriasdev.dds.domain.BloodType;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class BloodCompatibilityService {

    private static final Map<BloodType, Set<BloodType>> DONOR_TO_RECIPIENTS = new EnumMap<>(BloodType.class);

    static {
        // Red blood cell donation compatibility (donor -> recipients who can receive)
        DONOR_TO_RECIPIENTS.put(BloodType.O_NEG, EnumSet.allOf(BloodType.class));
        DONOR_TO_RECIPIENTS.put(BloodType.O_POS, EnumSet.of(
                BloodType.O_POS, BloodType.A_POS, BloodType.B_POS, BloodType.AB_POS
        ));
        DONOR_TO_RECIPIENTS.put(BloodType.A_NEG, EnumSet.of(
                BloodType.A_NEG, BloodType.A_POS, BloodType.AB_NEG, BloodType.AB_POS
        ));
        DONOR_TO_RECIPIENTS.put(BloodType.A_POS, EnumSet.of(
                BloodType.A_POS, BloodType.AB_POS
        ));
        DONOR_TO_RECIPIENTS.put(BloodType.B_NEG, EnumSet.of(
                BloodType.B_NEG, BloodType.B_POS, BloodType.AB_NEG, BloodType.AB_POS
        ));
        DONOR_TO_RECIPIENTS.put(BloodType.B_POS, EnumSet.of(
                BloodType.B_POS, BloodType.AB_POS
        ));
        DONOR_TO_RECIPIENTS.put(BloodType.AB_NEG, EnumSet.of(
                BloodType.AB_NEG, BloodType.AB_POS
        ));
        DONOR_TO_RECIPIENTS.put(BloodType.AB_POS, EnumSet.of(
                BloodType.AB_POS
        ));
    }

    public boolean canDonateTo(BloodType donor, BloodType recipient) {
        if (donor == null || recipient == null) {
            return false;
        }
        return DONOR_TO_RECIPIENTS.getOrDefault(donor, Set.of()).contains(recipient);
    }

    public List<BloodType> getCompatibleDonorTypes(BloodType recipient) {
        if (recipient == null) {
            return List.of();
        }
        return java.util.Arrays.stream(BloodType.values())
                .filter(donor -> canDonateTo(donor, recipient))
                .toList();
    }

    public List<BloodType> getCompatibleRecipientTypes(BloodType donor) {
        if (donor == null) {
            return List.of();
        }
        return DONOR_TO_RECIPIENTS.getOrDefault(donor, Set.of()).stream().sorted().toList();
    }
}

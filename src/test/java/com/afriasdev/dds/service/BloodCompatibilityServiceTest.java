package com.afriasdev.dds.service;

import com.afriasdev.dds.domain.BloodType;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class BloodCompatibilityServiceTest {

    private final BloodCompatibilityService service = new BloodCompatibilityService();

    @Test
    void canDonateTo_covers_all_combinations() {
        for (BloodType donor : BloodType.values()) {
            for (BloodType recipient : BloodType.values()) {
                boolean expected = expectedCompatible(donor, recipient);
                assertThat(service.canDonateTo(donor, recipient))
                        .as("%s -> %s", donor.getCode(), recipient.getCode())
                        .isEqualTo(expected);
            }
        }
    }

    @Test
    void getCompatibleDonorTypes_are_exhaustive_and_consistent() {
        for (BloodType recipient : BloodType.values()) {
            List<BloodType> donors = service.getCompatibleDonorTypes(recipient);
            assertThat(donors).isNotEmpty();
            for (BloodType donor : BloodType.values()) {
                if (donors.contains(donor)) {
                    assertThat(service.canDonateTo(donor, recipient)).isTrue();
                } else {
                    assertThat(service.canDonateTo(donor, recipient)).isFalse();
                }
            }
        }
    }

    @Test
    void getCompatibleRecipientTypes_are_exhaustive_and_consistent() {
        for (BloodType donor : BloodType.values()) {
            List<BloodType> recipients = service.getCompatibleRecipientTypes(donor);
            assertThat(recipients).isNotEmpty();
            for (BloodType recipient : BloodType.values()) {
                if (recipients.contains(recipient)) {
                    assertThat(service.canDonateTo(donor, recipient)).isTrue();
                } else {
                    assertThat(service.canDonateTo(donor, recipient)).isFalse();
                }
            }
        }
    }

    @Test
    void oNeg_is_universal_donor_and_abPos_is_universal_recipient() {
        for (BloodType recipient : BloodType.values()) {
            assertThat(service.canDonateTo(BloodType.O_NEG, recipient)).isTrue();
        }
        for (BloodType donor : BloodType.values()) {
            assertThat(service.canDonateTo(donor, BloodType.AB_POS)).isTrue();
        }
    }

    private boolean expectedCompatible(BloodType donor, BloodType recipient) {
        return switch (donor) {
            case O_NEG -> true;
            case O_POS -> Set.of(BloodType.O_POS, BloodType.A_POS, BloodType.B_POS, BloodType.AB_POS).contains(recipient);
            case A_NEG -> Set.of(BloodType.A_NEG, BloodType.A_POS, BloodType.AB_NEG, BloodType.AB_POS).contains(recipient);
            case A_POS -> Set.of(BloodType.A_POS, BloodType.AB_POS).contains(recipient);
            case B_NEG -> Set.of(BloodType.B_NEG, BloodType.B_POS, BloodType.AB_NEG, BloodType.AB_POS).contains(recipient);
            case B_POS -> Set.of(BloodType.B_POS, BloodType.AB_POS).contains(recipient);
            case AB_NEG -> Set.of(BloodType.AB_NEG, BloodType.AB_POS).contains(recipient);
            case AB_POS -> recipient == BloodType.AB_POS;
        };
    }
}

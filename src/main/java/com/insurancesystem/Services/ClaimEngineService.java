package com.insurancesystem.Services;

import com.insurancesystem.Model.Entity.Client;
import com.insurancesystem.Model.Entity.Coverage;
import com.insurancesystem.Model.Entity.FamilyMember;
import com.insurancesystem.Model.Entity.HealthcareProviderClaim;
import com.insurancesystem.Model.Entity.Policy;
import com.insurancesystem.Model.Entity.Enums.AllowedGender;
import com.insurancesystem.Repository.ClientRepository;
import com.insurancesystem.Repository.CoverageRepository;
import com.insurancesystem.Repository.FamilyMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.Optional;
@Service
@RequiredArgsConstructor
public class ClaimEngineService {

    private final CoverageRepository coverageRepo;
    private final ClientRepository clientRepo;
    private final FamilyMemberRepository familyMemberRepo;

    public HealthcareProviderClaim applyCoverageRules(HealthcareProviderClaim claim) {

        Policy policy = claim.getPolicy();

        // 1) العثور على التغطية المناسبة
        Coverage cov = coverageRepo.findByPolicy(policy).stream()
                .filter(c -> c.getServiceName().equalsIgnoreCase(claim.getDescription()))
                .findFirst()
                .orElse(null);

        // 2) الخدمة غير مغطاة
        if (cov == null) {
            claim.setIsCovered(false);
            claim.setCoverageMessage("❌ Service not covered under this policy");
            claim.setInsuranceCoveredAmount(BigDecimal.ZERO);
            claim.setClientPayAmount(BigDecimal.valueOf(claim.getAmount()));
            return claim;
        }

        // 3) غير مفعلة
        if (!cov.isCovered()) {
            claim.setIsCovered(false);
            claim.setCoverageMessage("❌ This service is excluded");
            return claim;
        }

        // 3.1) Gender validation
        String patientGender = getPatientGender(claim);
        Integer patientAge = getPatientAge(claim);
        if (!isGenderAllowed(patientGender, cov.getAllowedGender(), patientAge)) {
            claim.setIsCovered(false);
            claim.setCoverageMessage("❌ This service is not available for patient's gender/age category");
            claim.setInsuranceCoveredAmount(BigDecimal.ZERO);
            claim.setClientPayAmount(BigDecimal.valueOf(claim.getAmount()));
            return claim;
        }

        // 3.2) Age validation
        if (!isAgeAllowed(patientAge, cov.getMinAge(), cov.getMaxAge())) {
            claim.setIsCovered(false);
            claim.setCoverageMessage("❌ Patient age (" + patientAge + ") is outside coverage range (" +
                    (cov.getMinAge() != null ? cov.getMinAge() : "0") + "-" +
                    (cov.getMaxAge() != null ? cov.getMaxAge() : "unlimited") + ")");
            claim.setInsuranceCoveredAmount(BigDecimal.ZERO);
            claim.setClientPayAmount(BigDecimal.valueOf(claim.getAmount()));
            return claim;
        }

        BigDecimal amount = BigDecimal.valueOf(claim.getAmount());

        // 4) requires referral
        if (cov.isRequiresReferral() && claim.getDoctorName() == null) {
            claim.setIsCovered(false);
            claim.setCoverageMessage("❌ Requires referral from doctor");
            return claim;
        }

        // 5) minimum deductible
        if (amount.compareTo(cov.getMinimumDeductible()) < 0) {
            claim.setIsCovered(false);
            claim.setCoverageMessage("❌ Below minimum deductible: " + cov.getMinimumDeductible());
            claim.setClientPayAmount(amount);
            return claim;
        }

        // 6) emergency full coverage
        if (claim.getEmergency() != null && claim.getEmergency() && cov.isEmergencyEligible()) {

            claim.setIsCovered(true);
            claim.setCoverageMessage("🚑 Emergency — fully covered");

            claim.setInsuranceCoveredAmount(amount);
            claim.setClientPayAmount(BigDecimal.ZERO);
            claim.setCoveragePercentUsed(BigDecimal.valueOf(100));
            claim.setMaxCoverageUsed(BigDecimal.ZERO);

            return claim;
        }

        // 7) normal coverage percent
        BigDecimal percent = cov.getCoveragePercent().divide(BigDecimal.valueOf(100));
        BigDecimal insurancePay = amount.multiply(percent);
        BigDecimal clientPay = amount.subtract(insurancePay);

        // 8) max limit
        if (cov.getMaxLimit().compareTo(BigDecimal.ZERO) > 0 &&
                insurancePay.compareTo(cov.getMaxLimit()) > 0) {

            insurancePay = cov.getMaxLimit();
            clientPay = amount.subtract(insurancePay);
        }

        // 9) fill claim
        claim.setIsCovered(true);
        claim.setCoverageMessage("✔ Covered with rules");

        claim.setInsuranceCoveredAmount(insurancePay);
        claim.setClientPayAmount(clientPay);
        claim.setCoveragePercentUsed(cov.getCoveragePercent());
        claim.setMaxCoverageUsed(cov.getMaxLimit());

        return claim;
    }

    // ===========================
    // Helper Methods for Coverage Rules
    // ===========================

    private String getPatientGender(HealthcareProviderClaim claim) {
        if (claim.getClientId() == null) {
            return null;
        }

        // First try to find as a family member
        Optional<FamilyMember> familyMemberOpt = familyMemberRepo.findById(claim.getClientId());
        if (familyMemberOpt.isPresent()) {
            FamilyMember fm = familyMemberOpt.get();
            return fm.getGender() != null ? fm.getGender().toString() : null;
        }

        // Then try as a client
        Optional<Client> clientOpt = clientRepo.findById(claim.getClientId());
        if (clientOpt.isPresent()) {
            return clientOpt.get().getGender();
        }

        return null;
    }

    private Integer getPatientAge(HealthcareProviderClaim claim) {
        if (claim.getClientId() == null) {
            return null;
        }

        LocalDate dateOfBirth = null;

        // First try to find as a family member
        Optional<FamilyMember> familyMemberOpt = familyMemberRepo.findById(claim.getClientId());
        if (familyMemberOpt.isPresent()) {
            dateOfBirth = familyMemberOpt.get().getDateOfBirth();
        } else {
            // Then try as a client
            Optional<Client> clientOpt = clientRepo.findById(claim.getClientId());
            if (clientOpt.isPresent()) {
                dateOfBirth = clientOpt.get().getDateOfBirth();
            }
        }

        if (dateOfBirth != null) {
            return Period.between(dateOfBirth, LocalDate.now()).getYears();
        }
        return null;
    }

    private boolean isGenderAllowed(String patientGender, AllowedGender allowedGender, Integer patientAge) {
        if (patientGender == null || allowedGender == null || allowedGender == AllowedGender.ALL) {
            return true;
        }
        switch (allowedGender) {
            case MALE:
                return "MALE".equalsIgnoreCase(patientGender);
            case FEMALE:
                return "FEMALE".equalsIgnoreCase(patientGender);
            case CHILD:
                return patientAge != null && patientAge < 18;
            default:
                return true;
        }
    }

    private boolean isAgeAllowed(Integer patientAge, Integer minAge, Integer maxAge) {
        // If no age restrictions, allow
        if (minAge == null && maxAge == null) {
            return true;
        }
        // If patient age is unknown, allow (cannot validate)
        if (patientAge == null) {
            return true;
        }
        // Check min age
        if (minAge != null && patientAge < minAge) {
            return false;
        }
        // Check max age
        if (maxAge != null && patientAge > maxAge) {
            return false;
        }
        return true;
    }
}

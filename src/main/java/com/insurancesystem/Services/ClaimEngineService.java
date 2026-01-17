package com.insurancesystem.Services;

import com.insurancesystem.Model.Entity.Coverage;
import com.insurancesystem.Model.Entity.HealthcareProviderClaim;
import com.insurancesystem.Model.Entity.Policy;
import com.insurancesystem.Repository.CoverageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
@Service
@RequiredArgsConstructor
public class ClaimEngineService {

    private final CoverageRepository coverageRepo;

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
}

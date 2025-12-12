package com.insurancesystem.Model.MapStruct;

import com.insurancesystem.Model.Dto.HealthcareProviderClaimDTO;
import com.insurancesystem.Model.Dto.CreateHealthcareProviderClaimDTO;
import com.insurancesystem.Model.Dto.HealthcareProviderClaimMedicalDTO;
import com.insurancesystem.Model.Entity.HealthcareProviderClaim;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface HealthcareProviderClaimMapper {

    // CREATE → ENTITY
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "healthcareProvider", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "invoiceImagePath", ignore = true)
    @Mapping(target = "approvedAt", ignore = true)
    @Mapping(target = "rejectedAt", ignore = true)
    @Mapping(target = "rejectionReason", ignore = true)
    @Mapping(source = "diagnosis", target = "diagnosis")
    @Mapping(source = "treatmentDetails", target = "treatmentDetails")
    @Mapping(target = "submittedAt", expression = "java(java.time.Instant.now())")
    HealthcareProviderClaim toEntity(CreateHealthcareProviderClaimDTO dto);


    // BASIC DTO
    @Mapping(source = "healthcareProvider.id", target = "providerId")
    @Mapping(source = "healthcareProvider.fullName", target = "providerName")
    @Mapping(source = "diagnosis", target = "diagnosis")
    @Mapping(source = "treatmentDetails", target = "treatmentDetails")
    HealthcareProviderClaimDTO toDto(HealthcareProviderClaim claim);


    // MEDICAL REVIEW DTO
    @Mapping(source = "healthcareProvider.id", target = "providerId")
    @Mapping(source = "healthcareProvider.fullName", target = "providerName")
    @Mapping(source = "diagnosis", target = "diagnosis")
    @Mapping(source = "treatmentDetails", target = "treatmentDetails")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "serviceDate", target = "serviceDate")
    @Mapping(source = "invoiceImagePath", target = "invoiceImagePath")
    @Mapping(source = "clientId", target = "clientId")
    @Mapping(source = "medicalReviewerName", target = "medicalReviewerName")
    @Mapping(target = "medicalReviewedAt",
            expression = "java(claim.getMedicalReviewedAt() != null ? claim.getMedicalReviewedAt().toString() : null)")
    HealthcareProviderClaimMedicalDTO toMedicalDto(HealthcareProviderClaim claim);

}

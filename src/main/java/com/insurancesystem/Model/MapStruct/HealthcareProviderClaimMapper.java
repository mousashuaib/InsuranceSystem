package com.insurancesystem.Model.MapStruct;

import com.insurancesystem.Model.Dto.HealthcareProviderClaimDTO;
import com.insurancesystem.Model.Dto.CreateHealthcareProviderClaimDTO;
import com.insurancesystem.Model.Dto.HealthcareProviderClaimMedicalDTO;
import com.insurancesystem.Model.Entity.HealthcareProviderClaim;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface HealthcareProviderClaimMapper {

<<<<<<< HEAD
    // Entity → DTO
    @Mapping(source = "healthcareProvider.id", target = "providerId")
    @Mapping(target = "providerName", expression = "java(entity.getProviderName() != null ? entity.getProviderName() : entity.getHealthcareProvider() != null ? entity.getHealthcareProvider().getFullName() : null)")
    @Mapping(source = "clientName", target = "clientName")
    @Mapping(source = "clientId", target = "clientId")
    // New fields are mapped automatically by name
    HealthcareProviderClaimDTO toDto(HealthcareProviderClaim entity);

    // CreateDTO → Entity
=======
    // CREATE → ENTITY
>>>>>>> 59fc73de7f549007a5658aab4146b5707a8a4bd8
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "healthcareProvider", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "invoiceImagePath", ignore = true)
    @Mapping(target = "approvedAt", ignore = true)
    @Mapping(target = "rejectedAt", ignore = true)
    @Mapping(target = "rejectionReason", ignore = true)
<<<<<<< HEAD
    @Mapping(target = "invoiceImagePath", ignore = true)
    @Mapping(target = "medicalReviewedAt", ignore = true)
    @Mapping(target = "clientId", source = "clientId")
    @Mapping(target = "roleSpecificData", source = "roleSpecificData")
=======
    @Mapping(source = "diagnosis", target = "diagnosis")
    @Mapping(source = "treatmentDetails", target = "treatmentDetails")
    @Mapping(target = "submittedAt", expression = "java(java.time.Instant.now())")
>>>>>>> 59fc73de7f549007a5658aab4146b5707a8a4bd8
    HealthcareProviderClaim toEntity(CreateHealthcareProviderClaimDTO dto);


    // BASIC DTO
    @Mapping(source = "healthcareProvider.id", target = "providerId")
    @Mapping(source = "healthcareProvider.fullName", target = "providerName")
    @Mapping(source = "diagnosis", target = "diagnosis")
    @Mapping(source = "treatmentDetails", target = "treatmentDetails")
    // ✅ المهم
    @Mapping(target = "medicalReviewerName", source = "medicalReviewerName")
    @Mapping(target = "medicalReviewedAt", source = "medicalReviewedAt")
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
    @Mapping(source = "medicalReviewedAt", target = "medicalReviewedAt")
    @Mapping(source = "isFollowUp", target = "isFollowUp")
    @Mapping(source = "originalConsultationFee", target = "originalConsultationFee")
    HealthcareProviderClaimMedicalDTO toMedicalDto(HealthcareProviderClaim claim);

}

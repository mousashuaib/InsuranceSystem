package com.insurancesystem.Model.Dto;

import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RadiologyRequestDTO {

    private UUID id;  // Unique identifier for the radiology request

    private String testName;  // Type of radiology test (e.g., X-ray, MRI, CT scan, etc.)

    private String notes;  // Notes or comments from the doctor requesting the radiology test

    private String resultUrl;  // URL or path to the result file from the radiology department

    private String status;  // Status of the request (e.g., PENDING, COMPLETED)

    private UUID doctorId;  // ID of the doctor who created the radiology request
    private String doctorName;  // Name of the doctor who created the radiology request

    private UUID memberId;  // ID of the member (patient) for whom the radiology test is requested
    private String memberName;  // Name of the member (patient)

    private UUID radiologistId;  // ID of the radiologist handling the request
    private String radiologistName;  // Name of the radiologist handling the request

    private long total;  // Total number of radiology requests (could be useful for reporting)
    private long pending;  // Number of pending requests
    private long completed;  // Number of completed requests

    private Instant createdAt;  // Timestamp when the radiology request was created
    private Instant updatedAt;  // Timestamp when the radiology request was last updated

}

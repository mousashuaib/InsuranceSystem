package com.insurancesystem.Exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(NotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
            "success", false,
            "error", ex.getMessage()
        ));
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequest(BadRequestException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
            "success", false,
            "error", ex.getMessage()
        ));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrity(DataIntegrityViolationException ex) {
        String message = "Cannot delete: This record is referenced by other data in the system.";

        // Provide more specific message if possible
        String rootMessage = ex.getMostSpecificCause().getMessage();
        if (rootMessage != null) {
            if (rootMessage.contains("policies") || rootMessage.contains("claims")) {
                message = "Cannot delete policy: This policy has associated claims. Please delete or reassign the claims first.";
            } else if (rootMessage.contains("clients")) {
                message = "Cannot delete policy: Some clients are still assigned to this policy.";
            } else if (rootMessage.contains("doctor_medicine_assignments")) {
                message = "Cannot delete: This record has associated medicine assignments.";
            } else if (rootMessage.contains("coverages")) {
                message = "Cannot delete: This policy has associated coverages.";
            } else if (rootMessage.contains("healthcare_provider_claims")) {
                message = "Cannot delete: This record has associated healthcare claims.";
            }
        }

        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
            "success", false,
            "error", message
        ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneral(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
            "success", false,
            "error", "An unexpected error occurred: " + ex.getMessage()
        ));
    }
}

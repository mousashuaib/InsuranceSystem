package com.insurancesystem.Controller;

import com.insurancesystem.Exception.BadRequestException;
import com.insurancesystem.Model.Dto.ClientDto;
import com.insurancesystem.Model.Dto.VerifyEmailRequest;
import com.insurancesystem.Model.Dto.auth.*;
import com.insurancesystem.Security.JwtService;
import com.insurancesystem.Services.AuthService;
import com.insurancesystem.Services.ClientServices;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final ClientServices clientServices;
    private final JwtService jwtService;

    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RegisterResponse> register(
            @RequestPart("data") String reqJson,
            @RequestPart(value = "universityCard", required = false) MultipartFile[] universityCard,
            @RequestPart(value = "familyDocuments", required = false) MultipartFile[] familyDocuments,
            @RequestPart(value = "chronicDocuments", required = false) MultipartFile[] chronicDocuments,
            @RequestPart(value = "familyDocumentsOwners", required = false) String familyDocumentsOwnersJson

            ) {
        var out = authService.register(
                reqJson,
                universityCard,
                familyDocuments,
                chronicDocuments,
                familyDocumentsOwnersJson,
                false
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(out);
    }

    // Simple JSON-based registration for testing/API clients without file uploads
    @PostMapping(value = "/register-simple", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RegisterResponse> registerSimple(@RequestBody String reqJson) {
        var out = authService.register(
                reqJson,
                null,
                null,
                null,
                null,
                false
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(out);
    }

    @PreAuthorize("hasAuthority('ROLE_INSURANCE_MANAGER')")
    @PostMapping(value = "/admin/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RegisterResponse> registerByAdmin(
            @RequestPart("data") String reqJson,
            @RequestPart(value = "universityCard", required = false) MultipartFile[] universityCard,
            @RequestPart(value = "familyDocuments", required = false) MultipartFile[] familyDocuments,
            @RequestPart(value = "chronicDocuments", required = false) MultipartFile[] chronicDocuments,
            @RequestPart(value = "familyDocumentsOwners", required = false) String familyDocumentsOwnersJson

    ) {

        var out = authService.register(
                reqJson,
                universityCard,
                familyDocuments,
                chronicDocuments,
                familyDocumentsOwnersJson,
                true
        );


        return ResponseEntity.status(HttpStatus.CREATED).body(out);
    }



    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req) {
        try {
            var out = authService.login(req);
            return ResponseEntity.ok(out);
        } catch (BadRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(java.util.Map.of("message", e.getMessage()));
        }
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/me")
    public ResponseEntity<ClientDto> me() {
        String email = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication().getName();

        return ResponseEntity.ok(clientServices.getByEmail(email));

    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader != null && authHeader.regionMatches(true, 0, "Bearer ", 0, 7)) {
            String token = authHeader.substring(7).trim();
            if (!token.isEmpty()) {
                jwtService.revoke(token); // شطب مؤقت حتى انتهاء الصلاحية
            }
        }
        return ResponseEntity.noContent().build(); // 204
    }

    // ✅ Forgot Password (Web + Mobile)
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest req,
            @RequestHeader(value = "X-Client-Type", defaultValue = "WEB") String clientType) {

        boolean isMobile = clientType.equalsIgnoreCase("MOBILE");

        authService.initiatePasswordReset(req.getEmail(), isMobile);

        return ResponseEntity.ok(
                isMobile ? "Password reset link sent to your mobile email"
                        : "Password reset link sent to your web email"
        );
    }


    // ✅ Reset Password
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@Valid @RequestBody ResetPasswordRequest req) {
        authService.resetPassword(req.getToken(), req.getNewPassword());
        return ResponseEntity.ok("Password has been reset successfully");
    }

    @PostMapping("/verify-email")
    public ResponseEntity<String> verifyEmail(
            @RequestBody VerifyEmailRequest req) {

        authService.verifyEmail(req.getEmail(), req.getCode());
        return ResponseEntity.ok("Email verified successfully");
    }

    // DEBUG: Check what authorities are being loaded for the current user
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/debug/authorities")
    public ResponseEntity<?> debugAuthorities() {
        var auth = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication();

        java.util.Map<String, Object> debug = new java.util.HashMap<>();
        debug.put("principal", auth.getName());
        debug.put("authorities", auth.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .toList());
        debug.put("authenticated", auth.isAuthenticated());
        debug.put("details", auth.getDetails() != null ? auth.getDetails().toString() : null);

        return ResponseEntity.ok(debug);
    }

}
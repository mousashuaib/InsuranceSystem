package com.insurancesystem.Controller;

import com.insurancesystem.Model.Dto.CreateMemberEnrollmentDTO;
import com.insurancesystem.Model.Dto.MemberEnrollmentDTO;
import com.insurancesystem.Model.Dto.UpdateMemberEnrollmentDTO;
import com.insurancesystem.Services.MemberEnrollmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor
public class MemberEnrollmentController {

    private final MemberEnrollmentService enrollmentService;

    // POST /api/enrollments  — Create Enrollment
    @PostMapping
    public ResponseEntity<MemberEnrollmentDTO> create(@Valid @RequestBody CreateMemberEnrollmentDTO dto) {
        MemberEnrollmentDTO created = enrollmentService.create(dto);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.getId()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    // GET /api/enrollments/by-member?memberId={UUID}  — List Enrollments for a Member
    @GetMapping("/by-member")
    public ResponseEntity<List<MemberEnrollmentDTO>> listByMember(@RequestParam UUID memberId) {
        return ResponseEntity.ok(enrollmentService.listByMember(memberId));
    }

    // GET /api/enrollments/{id} — Get Enrollment by ID
    @GetMapping("/{id}")
    public ResponseEntity<MemberEnrollmentDTO> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(enrollmentService.getById(id));
    }

    // PATCH /api/enrollments/{id}  — Update Enrollment
    @PatchMapping("/{id}")
    public ResponseEntity<MemberEnrollmentDTO> update(@PathVariable UUID id,
                                                      @Valid @RequestBody UpdateMemberEnrollmentDTO dto) {
        return ResponseEntity.ok(enrollmentService.update(id, dto));
    }

    // DELETE /api/enrollments/{id} — Delete Enrollment
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        enrollmentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

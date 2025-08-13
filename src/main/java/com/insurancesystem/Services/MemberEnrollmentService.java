package com.insurancesystem.Services;

import com.insurancesystem.Exception.BadRequestException;
import com.insurancesystem.Exception.NotFoundException;
import com.insurancesystem.Model.Dto.CreateMemberEnrollmentDTO;
import com.insurancesystem.Model.Dto.MemberEnrollmentDTO;
import com.insurancesystem.Model.Dto.UpdateMemberEnrollmentDTO;
import com.insurancesystem.Model.Entity.Client;
import com.insurancesystem.Model.Entity.MemberEnrollment;
import com.insurancesystem.Model.Entity.Policy;
import com.insurancesystem.Model.MapStruct.MemberEnrollmentMapper;
import com.insurancesystem.Repository.MemberEnrollmentRepository;
import com.insurancesystem.Repository.PolicyRepository;
import com.insurancesystem.Repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberEnrollmentService {

    private final MemberEnrollmentRepository enrollmentRepo;
    private final ClientRepository userRepo;
    private final PolicyRepository policyRepo;
    private final MemberEnrollmentMapper memberEnrollmentMapper;

    public MemberEnrollmentDTO create(CreateMemberEnrollmentDTO dto) {
        Client member = userRepo.findById(dto.getUserId())
                .orElseThrow(() -> new NotFoundException("User not found"));
        Policy policy = policyRepo.findById(dto.getPolicyId())
                .orElseThrow(() -> new NotFoundException("Policy not found"));

        if (enrollmentRepo.existsByMemberAndPolicyAndStartDate(member, policy, dto.getStartDate())) {
            throw new BadRequestException("Enrollment already exists for this date");
        }

        MemberEnrollment entity = memberEnrollmentMapper.toEntity(dto);
        entity.setMember(member);
        entity.setPolicy(policy);
        return memberEnrollmentMapper.toDTO(enrollmentRepo.save(entity));
    }

    @Transactional(readOnly = true)
    public List<MemberEnrollmentDTO> listByMember(UUID memberId) {
        Client member = userRepo.findById(memberId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        return enrollmentRepo.findByMember(member)
                .stream().map(memberEnrollmentMapper::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public MemberEnrollmentDTO getById(UUID id) {
        MemberEnrollment entity = enrollmentRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Enrollment not found"));
        return memberEnrollmentMapper.toDTO(entity);
    }

    public MemberEnrollmentDTO update(UUID id, UpdateMemberEnrollmentDTO dto) {
        MemberEnrollment entity = enrollmentRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Enrollment not found"));

        // تحقّق اختياري: endDate ما تكون قبل startDate الحالي
        if (dto.getEndDate() != null) {
            LocalDate start = entity.getStartDate();
            if (start != null && dto.getEndDate().isBefore(start)) {
                throw new BadRequestException("endDate cannot be before startDate");
            }
        }

        memberEnrollmentMapper.updateEntityFromDTO(dto, entity);
        return memberEnrollmentMapper.toDTO(enrollmentRepo.save(entity));
    }

    public void delete(UUID id) {
        if (!enrollmentRepo.existsById(id)) {
            throw new NotFoundException("Enrollment not found");
        }
        enrollmentRepo.deleteById(id);
    }
}

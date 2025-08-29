package com.insurancesystem.Services;

import com.insurancesystem.Exception.BadRequestException;
import com.insurancesystem.Exception.NotFoundException;
import com.insurancesystem.Model.Dto.CoverageDTO;
import com.insurancesystem.Model.Dto.CreateCoverageDTO;
import com.insurancesystem.Model.Dto.UpdateCoverageDTO;
import com.insurancesystem.Model.Entity.Coverage;
import com.insurancesystem.Model.Entity.Policy;
import com.insurancesystem.Model.MapStruct.CoverageMapper;
import com.insurancesystem.Repository.CoverageRepository;
import com.insurancesystem.Repository.PolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class CoverageService {

    private final CoverageRepository coverageRepo;
    private final PolicyRepository policyRepo;
    private final CoverageMapper coverageMapper;

    public CoverageDTO add(CreateCoverageDTO dto) {
        Policy policy = policyRepo.findById(dto.getPolicyId())
                .orElseThrow(() -> new NotFoundException("Policy not found"));

        if (coverageRepo.existsByPolicyAndServiceNameIgnoreCase(policy, dto.getServiceName()))
            throw new BadRequestException("Service already exists for this policy");

        Coverage entity = coverageMapper.toEntity(dto);
        entity.setPolicy(policy);
        return coverageMapper.toDTO(coverageRepo.save(entity));
    }

    @Transactional(readOnly = true)
    public List<CoverageDTO> listByPolicy(UUID policyId) {
        Policy policy = policyRepo.findById(policyId)
                .orElseThrow(() -> new NotFoundException("Policy not found"));
        return coverageRepo.findByPolicy(policy).stream().map(coverageMapper::toDTO).toList();
    }

    public CoverageDTO update(UUID id, UpdateCoverageDTO dto) {
        Coverage entity = coverageRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Coverage not found"));
        coverageMapper.updateEntityFromDTO(dto, entity);
        return coverageMapper.toDTO(coverageRepo.save(entity));
    }

    public void delete(UUID id) {
        if (!coverageRepo.existsById(id)) throw new NotFoundException("Coverage not found");
        coverageRepo.deleteById(id);
    }
}

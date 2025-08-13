package com.insurancesystem.Services;

import com.insurancesystem.Exception.BadRequestException;
import com.insurancesystem.Exception.NotFoundException;
import com.insurancesystem.Model.Dto.CreatePolicyDTO;
import com.insurancesystem.Model.Dto.PolicyDTO;
import com.insurancesystem.Model.Dto.UpdatePolicyDTO;
import com.insurancesystem.Model.Entity.Policy;
import com.insurancesystem.Model.MapStruct.PolicyMapper;
import com.insurancesystem.Repository.PolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class PolicyService {

    private final PolicyRepository policyRepo;
    private final PolicyMapper policyMapper;

    public PolicyDTO create(CreatePolicyDTO dto) {
        policyRepo.findByPolicyNo(dto.getPolicyNo()).ifPresent(p -> {
            throw new BadRequestException("Policy number already exists");
        });
        Policy entity = policyMapper.toEntity(dto);
        return policyMapper.toDTO(policyRepo.save(entity));
    }

    @Transactional(readOnly = true)
    public PolicyDTO get(UUID id) {
        Policy p = policyRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Policy not found"));
        return policyMapper.toDTO(p);
    }

    @Transactional(readOnly = true)
    public List<PolicyDTO> list() {
        return policyRepo.findAll().stream().map(policyMapper::toDTO).toList();
    }

    public PolicyDTO update(UUID id, UpdatePolicyDTO dto) {
        Policy entity = policyRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Policy not found"));
        policyMapper.updateEntityFromDTO(dto, entity);
        return policyMapper.toDTO(policyRepo.save(entity));
    }

    public void delete(UUID id) {
        if (!policyRepo.existsById(id)) throw new NotFoundException("Policy not found");
        policyRepo.deleteById(id);
    }
}

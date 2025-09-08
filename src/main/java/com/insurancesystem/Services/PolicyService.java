package com.insurancesystem.Services;

import com.insurancesystem.Exception.BadRequestException;
import com.insurancesystem.Exception.NotFoundException;
import com.insurancesystem.Model.Dto.CreatePolicyDTO;
import com.insurancesystem.Model.Dto.PolicyDTO;
import com.insurancesystem.Model.Dto.UpdatePolicyDTO;
import com.insurancesystem.Model.Entity.Client;
import com.insurancesystem.Model.Entity.Policy;
import com.insurancesystem.Model.MapStruct.PolicyMapper;
import com.insurancesystem.Repository.ClaimRepository;
import com.insurancesystem.Repository.ClientRepository;
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
public class PolicyService {

    private final PolicyRepository policyRepo;
    private final PolicyMapper policyMapper;
    private final ClientRepository clientRepo;
    private final CoverageRepository coverageRepository;
    private final ClaimRepository claimRepository;




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

    @Transactional
    public void delete(UUID id) {
        Policy policy = policyRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Policy not found"));

        // افصل العملاء عن البوليصة
        List<Client> clients = clientRepo.findByPolicy(policy);
        for (Client c : clients) {
            c.setPolicy(null);
            clientRepo.save(c);
        }
        claimRepository.deleteAllByPolicy(policy);


        // احذف الكفرجز المرتبطة
        coverageRepository.deleteAll(policy.getCoverages());

        // احذف البوليصة
        policyRepo.delete(policy);
    }

    public void assignPolicyToClient(UUID clientId, UUID policyId) {
        Policy policy = policyRepo.findById(policyId)
                .orElseThrow(() -> new NotFoundException("Policy not found"));

        Client client = clientRepo.findById(clientId)
                .orElseThrow(() -> new NotFoundException("Client not found"));

        client.setPolicy(policy);
        clientRepo.save(client);
    }

    public void assignPolicyByName(UUID clientId, String policyName) {
        Policy policy = policyRepo.findByName(policyName)
                .orElseThrow(() -> new NotFoundException("Policy not found: " + policyName));

        Client client = clientRepo.findById(clientId)
                .orElseThrow(() -> new NotFoundException("Client not found"));

        client.setPolicy(policy);
        clientRepo.save(client); // ⬅️ مهم جداً
    }

    public PolicyDTO getPolicyByUsername(String username) {
        Client client = clientRepo.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("Client not found"));

        if (client.getPolicy() == null) {
            throw new NotFoundException("No policy assigned to this client");
        }

        return policyMapper.toDTO(client.getPolicy());
    }
    public PolicyDTO getPolicyByUserId(UUID userId) {
        Client client = clientRepo.findById(userId)
                .orElseThrow(() -> new NotFoundException("Client not found"));

        if (client.getPolicy() == null) {
            throw new NotFoundException("No policy assigned to this client");
        }

        return policyMapper.toDTO(client.getPolicy());
    }


}

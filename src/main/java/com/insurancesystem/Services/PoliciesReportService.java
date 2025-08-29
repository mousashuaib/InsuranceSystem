package com.insurancesystem.Services;

import com.insurancesystem.Model.Dto.PoliciesReportDto;
import com.insurancesystem.Model.Entity.Enums.MemberStatus;
import com.insurancesystem.Model.Entity.Enums.PolicyStatus;
import com.insurancesystem.Repository.ClientRepository;
import com.insurancesystem.Repository.PolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PoliciesReportService {

    private final ClientRepository clientRepo;
    private final PolicyRepository policyRepo;

    public PoliciesReportDto generateReport() {
        long totalMembers = clientRepo.count();
        long activeMembers = clientRepo.countByStatus(MemberStatus.ACTIVE);
        long inactiveMembers = clientRepo.countByStatus(MemberStatus.INACTIVE);

        long activePolicies = policyRepo.countByStatus(PolicyStatus.ACTIVE);

        Map<String, Long> membersPerPolicy = clientRepo.findAll().stream()
                .filter(c -> c.getPolicy() != null)
                .collect(Collectors.groupingBy(
                        c -> c.getPolicy().getName(),
                        Collectors.counting()
                ));

        return PoliciesReportDto.builder()
                .totalMembers(totalMembers)
                .activeMembers(activeMembers)
                .inactiveMembers(inactiveMembers)
                .activePolicies(activePolicies)
                .membersPerPolicy(membersPerPolicy)
                .build();
    }
}

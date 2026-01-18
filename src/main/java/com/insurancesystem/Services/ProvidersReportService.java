package com.insurancesystem.Services;

import com.insurancesystem.Model.Dto.ProvidersReportDto;
import com.insurancesystem.Model.Entity.Client;
import com.insurancesystem.Model.Entity.Enums.RoleName;
import com.insurancesystem.Repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProvidersReportService {

    private final ClientRepository clientRepo;

    public ProvidersReportDto generateReport() {
        List<Client> clients = clientRepo.findAll();

        // دكاترة - filter by requestedRole
        List<String> doctors = clients.stream()
                .filter(c -> c.getRequestedRole() == RoleName.DOCTOR)
                .map(Client::getFullName)
                .collect(Collectors.toList());

        // صيادلة
        List<String> pharmacies = clients.stream()
                .filter(c -> c.getRequestedRole() == RoleName.PHARMACIST)
                .map(Client::getFullName)
                .collect(Collectors.toList());

        // مختبرات
        List<String> labs = clients.stream()
                .filter(c -> c.getRequestedRole() == RoleName.LAB_TECH)
                .map(Client::getFullName)
                .collect(Collectors.toList());

        // أشعة (Radiologists)
        List<String> radiologists = clients.stream()
                .filter(c -> c.getRequestedRole() == RoleName.RADIOLOGIST)
                .map(Client::getFullName)
                .collect(Collectors.toList());

        return ProvidersReportDto.builder()
                .totalProviders(doctors.size() + pharmacies.size() + labs.size() + radiologists.size())
                .doctorsCount(doctors.size())
                .pharmaciesCount(pharmacies.size())
                .labsCount(labs.size())
                .doctors(doctors)
                .pharmacies(pharmacies)
                .labs(labs)
                .build();
    }
}

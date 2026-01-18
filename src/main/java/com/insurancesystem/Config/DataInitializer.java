package com.insurancesystem.Config;

import com.insurancesystem.Model.Entity.Client;
import com.insurancesystem.Model.Entity.FamilyMember;
import com.insurancesystem.Model.Entity.Role;
import com.insurancesystem.Model.Entity.Enums.FamilyRelation;
import com.insurancesystem.Model.Entity.Enums.Gender;
import com.insurancesystem.Model.Entity.Enums.MemberStatus;
import com.insurancesystem.Model.Entity.Enums.ProfileStatus;
import com.insurancesystem.Model.Entity.Enums.RoleName;
import com.insurancesystem.Model.Entity.Enums.RoleRequestStatus;
import com.insurancesystem.Repository.ClientRepository;
import com.insurancesystem.Repository.FamilyMemberRepository;
import com.insurancesystem.Repository.RoleRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    private final RoleRepository roleRepository;
    private final ClientRepository clientRepository;
    private final FamilyMemberRepository familyMemberRepository;
    private final PasswordEncoder passwordEncoder;

    @PostConstruct
    @Transactional
    public void init() {
        seedManagerAccount();
        seedApprovedDoctors();
        seedTestPendingRequests();
    }

    private void seedManagerAccount() {
        String managerEmail = "manager@insurance.com";
        String managerUsername = "manager";

        // Check if manager already exists by email
        if (clientRepository.findByEmail(managerEmail).isPresent()) {
            log.info("Manager account already exists with email: {}", managerEmail);
            return;
        }

        Role managerRole = roleRepository.findByName(RoleName.INSURANCE_MANAGER)
                .orElseThrow(() -> new RuntimeException("INSURANCE_MANAGER role not found"));

        // Check if there's an existing user with the username - if so, update it
        var existingByUsername = clientRepository.findByUsername(managerUsername);
        if (existingByUsername.isPresent()) {
            Client existing = existingByUsername.get();
            existing.setEmail(managerEmail);
            existing.setPasswordHash(passwordEncoder.encode("password123"));
            existing.setFullName("System Manager");
            existing.setStatus(MemberStatus.ACTIVE);
            existing.setRoleRequestStatus(RoleRequestStatus.APPROVED);
            existing.setEmailVerified(true);
            existing.getRoles().add(managerRole);
            clientRepository.save(existing);
            log.info("Updated existing manager account with email: {} and password: password123", managerEmail);
            return;
        }

        Set<Role> roles = new HashSet<>();
        roles.add(managerRole);

        Client manager = Client.builder()
                .email(managerEmail)
                .username(managerUsername)
                .passwordHash(passwordEncoder.encode("password123"))
                .fullName("System Manager")
                .phone("0512345678")
                .nationalId("123456789")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .gender("M")
                .status(MemberStatus.ACTIVE)
                .roleRequestStatus(RoleRequestStatus.APPROVED)
                .roles(roles)
                .emailVerified(true)
                .build();

        clientRepository.save(manager);
        log.info("Created manager account: {} with password: password123", managerEmail);
    }

    /**
     * Seeds approved doctors for Doctor Medicine Assignments feature
     */
    private void seedApprovedDoctors() {
        // Check if approved doctors already exist
        if (clientRepository.findByEmail("dr.ahmad.cardio@hospital.com").isPresent()) {
            log.info("Approved doctors already exist, skipping...");
            return;
        }

        log.info("Seeding approved doctors...");

        Role doctorRole = roleRepository.findByName(RoleName.DOCTOR)
                .orElseThrow(() -> new RuntimeException("DOCTOR role not found"));

        // Create multiple approved doctors with different specializations
        createApprovedDoctor(
                "dr.ahmad.cardio@hospital.com",
                "drahmadcardio",
                "Dr. Ahmad Nasser",
                "0591111111",
                "DOC111222",
                LocalDate.of(1980, 3, 15),
                "M",
                "Cardiology",
                "Medicine",
                "Cardiology Department",
                doctorRole
        );

        createApprovedDoctor(
                "dr.sara.general@hospital.com",
                "drsarageneral",
                "Dr. Sara Mahmoud",
                "0592222222",
                "DOC222333",
                LocalDate.of(1985, 7, 22),
                "F",
                "General Practice",
                "Medicine",
                "General Medicine",
                doctorRole
        );

        createApprovedDoctor(
                "dr.khaled.internal@hospital.com",
                "drkhaled",
                "Dr. Khaled Abu-Salem",
                "0593333333",
                "DOC333444",
                LocalDate.of(1978, 11, 8),
                "M",
                "Internal Medicine",
                "Medicine",
                "Internal Medicine Department",
                doctorRole
        );

        createApprovedDoctor(
                "dr.hana.neuro@hospital.com",
                "drhananeuro",
                "Dr. Hana Qassem",
                "0594444444",
                "DOC444555",
                LocalDate.of(1982, 5, 30),
                "F",
                "Neurology",
                "Medicine",
                "Neurology Department",
                doctorRole
        );

        createApprovedDoctor(
                "dr.youssef.endo@hospital.com",
                "dryoussefendo",
                "Dr. Youssef Barakat",
                "0595555555",
                "DOC555666",
                LocalDate.of(1975, 9, 12),
                "M",
                "Endocrinology",
                "Medicine",
                "Endocrinology & Diabetes",
                doctorRole
        );

        log.info("Approved doctors seeded successfully!");
    }

    private void createApprovedDoctor(
            String email,
            String username,
            String fullName,
            String phone,
            String nationalId,
            LocalDate dateOfBirth,
            String gender,
            String specialization,
            String faculty,
            String department,
            Role doctorRole
    ) {
        if (clientRepository.findByEmail(email).isPresent()) {
            log.info("Doctor {} already exists, skipping...", email);
            return;
        }

        Set<Role> roles = new HashSet<>();
        roles.add(doctorRole);

        Client doctor = Client.builder()
                .email(email)
                .username(username)
                .passwordHash(passwordEncoder.encode("password123"))
                .fullName(fullName)
                .phone(phone)
                .nationalId(nationalId)
                .employeeId("EMP" + nationalId.substring(3, 6))
                .dateOfBirth(dateOfBirth)
                .gender(gender)
                .specialization(specialization)
                .faculty(faculty)
                .department(department)
                .status(MemberStatus.ACTIVE)
                .roleRequestStatus(RoleRequestStatus.APPROVED)
                .requestedRole(RoleName.DOCTOR)
                .roles(roles)
                .emailVerified(true)
                .build();

        clientRepository.save(doctor);
        log.info("Created approved doctor: {} - {}", fullName, specialization);
    }

    /**
     * Seeds test data for pending requests (role requests and family members)
     */
    private void seedTestPendingRequests() {
        // Check if test data already exists
        if (clientRepository.findByEmail("test.doctor@example.com").isPresent()) {
            log.info("Test pending requests data already exists, skipping...");
            return;
        }

        log.info("Seeding test pending requests data...");

        // First, create an approved insurance client who will have family members
        Client approvedClient = createApprovedInsuranceClient();

        // Create pending role requests
        createPendingRoleRequests();

        // Create pending family members for the approved client
        createPendingFamilyMembers(approvedClient);

        log.info("Test pending requests data seeded successfully!");
    }

    private Client createApprovedInsuranceClient() {
        String email = "john.smith@example.com";

        if (clientRepository.findByEmail(email).isPresent()) {
            return clientRepository.findByEmail(email).get();
        }

        Role clientRole = roleRepository.findByName(RoleName.INSURANCE_CLIENT)
                .orElseThrow(() -> new RuntimeException("INSURANCE_CLIENT role not found"));

        Set<Role> roles = new HashSet<>();
        roles.add(clientRole);

        Client client = Client.builder()
                .email(email)
                .username("johnsmith")
                .passwordHash(passwordEncoder.encode("password123"))
                .fullName("John Smith")
                .phone("0501234567")
                .nationalId("987654321")
                .employeeId("EMP001")
                .dateOfBirth(LocalDate.of(1985, 5, 15))
                .gender("M")
                .status(MemberStatus.ACTIVE)
                .roleRequestStatus(RoleRequestStatus.APPROVED)
                .requestedRole(RoleName.INSURANCE_CLIENT)
                .roles(roles)
                .emailVerified(true)
                .build();

        client = clientRepository.save(client);
        log.info("Created approved insurance client: {}", email);
        return client;
    }

    private void createPendingRoleRequests() {
        // 1. Pending Doctor Request
        createPendingClient(
                "test.doctor@example.com",
                "testdoctor",
                "Dr. Ahmed Hassan",
                "0521111111",
                "111222333",
                LocalDate.of(1988, 3, 20),
                "M",
                RoleName.DOCTOR,
                "Cardiology",
                "Medicine",
                "Cardiology Department"
        );

        // 2. Pending Pharmacist Request
        createPendingClient(
                "test.pharmacist@example.com",
                "testpharmacist",
                "Sarah Johnson",
                "0522222222",
                "222333444",
                LocalDate.of(1992, 7, 10),
                "F",
                RoleName.PHARMACIST,
                null, null, null
        );

        // 3. Pending Lab Technician Request
        createPendingClient(
                "test.labtech@example.com",
                "testlabtech",
                "Mohammad Ali",
                "0523333333",
                "333444555",
                LocalDate.of(1995, 11, 5),
                "M",
                RoleName.LAB_TECH,
                null, null, null
        );

        // 4. Pending Radiologist Request
        createPendingClient(
                "test.radiologist@example.com",
                "testradiologist",
                "Dr. Fatima Khalil",
                "0524444444",
                "444555666",
                LocalDate.of(1990, 1, 25),
                "F",
                RoleName.RADIOLOGIST,
                null, null, null
        );

        // 5. Pending Insurance Client Request
        createPendingClient(
                "test.client@example.com",
                "testclient",
                "Omar Ibrahim",
                "0525555555",
                "555666777",
                LocalDate.of(1998, 9, 12),
                "M",
                RoleName.INSURANCE_CLIENT,
                null, null, null
        );

        // 6. Another Pending Doctor
        createPendingClient(
                "test.doctor2@example.com",
                "testdoctor2",
                "Dr. Layla Mansour",
                "0526666666",
                "666777888",
                LocalDate.of(1987, 4, 8),
                "F",
                RoleName.DOCTOR,
                "Pediatrics",
                "Medicine",
                "Pediatrics Department"
        );
    }

    private void createPendingClient(
            String email,
            String username,
            String fullName,
            String phone,
            String nationalId,
            LocalDate dateOfBirth,
            String gender,
            RoleName requestedRole,
            String specialization,
            String faculty,
            String department
    ) {
        if (clientRepository.findByEmail(email).isPresent()) {
            log.info("Pending client {} already exists, skipping...", email);
            return;
        }

        Client.ClientBuilder builder = Client.builder()
                .email(email)
                .username(username)
                .passwordHash(passwordEncoder.encode("password123"))
                .fullName(fullName)
                .phone(phone)
                .nationalId(nationalId)
                .dateOfBirth(dateOfBirth)
                .gender(gender)
                .status(MemberStatus.INACTIVE) // Pending users are inactive
                .roleRequestStatus(RoleRequestStatus.PENDING)
                .requestedRole(requestedRole)
                .roles(new HashSet<>()) // No roles until approved
                .emailVerified(true);

        // Add role-specific fields
        if (requestedRole == RoleName.DOCTOR) {
            builder.specialization(specialization)
                    .faculty(faculty)
                    .department(department);
        } else if (requestedRole == RoleName.PHARMACIST) {
            builder.pharmacyCode("PH001")
                    .pharmacyName("Care Plus Pharmacy")
                    .pharmacyLocation("Ramallah, Main Street");
        } else if (requestedRole == RoleName.LAB_TECH) {
            builder.labCode("LAB001")
                    .labName("City Lab Center")
                    .labLocation("Nablus, Medical District");
        } else if (requestedRole == RoleName.RADIOLOGIST) {
            builder.radiologyCode("RAD001")
                    .radiologyName("Advanced Imaging Center")
                    .radiologyLocation("Bethlehem, Hospital Road");
        } else if (requestedRole == RoleName.INSURANCE_CLIENT) {
            builder.employeeId("EMP" + nationalId.substring(0, 3));
        }

        Client client = builder.build();
        clientRepository.save(client);
        log.info("Created pending {} request: {}", requestedRole, email);
    }

    private void createPendingFamilyMembers(Client parentClient) {
        // Create pending family members
        createPendingFamilyMember(
                parentClient,
                "Mary Smith",
                "FM001001",
                parentClient.getEmployeeId() + ".01",
                FamilyRelation.WIFE,
                Gender.FEMALE,
                LocalDate.of(1988, 8, 20)
        );

        createPendingFamilyMember(
                parentClient,
                "James Smith",
                "FM001002",
                parentClient.getEmployeeId() + ".02",
                FamilyRelation.SON,
                Gender.MALE,
                LocalDate.of(2015, 3, 10)
        );

        createPendingFamilyMember(
                parentClient,
                "Emma Smith",
                "FM001003",
                parentClient.getEmployeeId() + ".03",
                FamilyRelation.DAUGHTER,
                Gender.FEMALE,
                LocalDate.of(2018, 6, 25)
        );

        createPendingFamilyMember(
                parentClient,
                "Robert Smith Sr.",
                "FM001004",
                parentClient.getEmployeeId() + ".04",
                FamilyRelation.FATHER,
                Gender.MALE,
                LocalDate.of(1955, 12, 1)
        );
    }

    private void createPendingFamilyMember(
            Client parentClient,
            String fullName,
            String nationalId,
            String insuranceNumber,
            FamilyRelation relation,
            Gender gender,
            LocalDate dateOfBirth
    ) {
        // Check if family member already exists
        if (familyMemberRepository.existsByNationalId(nationalId)) {
            log.info("Family member with nationalId {} already exists, skipping...", nationalId);
            return;
        }

        FamilyMember familyMember = FamilyMember.builder()
                .client(parentClient)
                .fullName(fullName)
                .nationalId(nationalId)
                .insuranceNumber(insuranceNumber)
                .relation(relation)
                .gender(gender)
                .dateOfBirth(dateOfBirth)
                .status(ProfileStatus.PENDING)
                .documentImages(List.of()) // Empty document list for test data
                .build();

        familyMemberRepository.save(familyMember);
        log.info("Created pending family member: {} ({})", fullName, relation);
    }
}

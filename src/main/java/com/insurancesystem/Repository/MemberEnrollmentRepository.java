package com.insurancesystem.Repository;

import com.insurancesystem.Model.Entity.Client;
import com.insurancesystem.Model.Entity.MemberEnrollment;
import com.insurancesystem.Model.Entity.Client;
import com.insurancesystem.Model.Entity.Policy;
import com.insurancesystem.Model.Entity.Enums.EnrollmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MemberEnrollmentRepository extends JpaRepository<MemberEnrollment, UUID> {
    List<MemberEnrollment> findByMember(Client member);
    List<MemberEnrollment> findByPolicy(Policy policy);
    Optional<MemberEnrollment> findFirstByMemberAndStatusOrderByStartDateDesc(Client member, EnrollmentStatus status);
    boolean existsByMemberAndPolicyAndStartDate(Client member, Policy policy, LocalDate startDate);
}

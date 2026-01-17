package com.insurancesystem.Repository;

import com.insurancesystem.Model.Entity.Client;
import com.insurancesystem.Model.Entity.Enums.MemberStatus;
import com.insurancesystem.Model.Entity.Enums.RoleName;
import com.insurancesystem.Model.Entity.FamilyMember;
import com.insurancesystem.Model.Entity.Policy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClientRepository extends JpaRepository<Client, UUID> {

    boolean existsByEmployeeId(String employeeId);

    Optional<Client> findByEmail(String email);

    boolean existsByNationalId(String nationalId);

    boolean existsByEmail(String email);

    List<Client> findByStatus(MemberStatus status);

    long countByStatus(MemberStatus status);

    List<Client> findByPolicy(Policy policy);

    Optional<Client> findByFullName(String fullName);

    List<Client> findByRoles_Name(RoleName roleName);

    Optional<Client> findByEmployeeId(String employeeId);

    @Query("""
        SELECT c FROM Client c
        WHERE
            (:fullName IS NULL OR LOWER(c.fullName) LIKE LOWER(CONCAT('%', :fullName, '%')))
        AND (:employeeId IS NULL OR c.employeeId = :employeeId)
        AND (:nationalId IS NULL OR c.nationalId = :nationalId)
        AND (:phone IS NULL OR c.phone = :phone)
    """)
    Optional<Client> findForCoordinatorClaim(
            @Param("fullName") String fullName,
            @Param("employeeId") String employeeId,
            @Param("nationalId") String nationalId,
            @Param("phone") String phone
    );

}

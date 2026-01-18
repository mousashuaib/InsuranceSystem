package com.insurancesystem.Repository;

import com.insurancesystem.Model.Entity.Client;
import com.insurancesystem.Model.Entity.Enums.MemberStatus;
import com.insurancesystem.Model.Entity.Enums.RoleName;
import com.insurancesystem.Model.Entity.FamilyMember;
import com.insurancesystem.Model.Entity.Policy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClientRepository extends JpaRepository<Client, UUID> {

    // Update policy_id to null for all clients with given policy - avoids cascade issues
    @Modifying
    @Query("UPDATE Client c SET c.policy = null WHERE c.policy.id = :policyId")
    void detachClientsFromPolicy(@Param("policyId") UUID policyId);

    boolean existsByEmployeeId(String employeeId);

    Optional<Client> findByEmail(String email);

    // Explicitly fetch roles to ensure they are loaded for authentication
    @Query("SELECT c FROM Client c LEFT JOIN FETCH c.roles WHERE LOWER(c.email) = LOWER(:email)")
    Optional<Client> findByEmailWithRoles(@Param("email") String email);

    Optional<Client> findByUsername(String username);

    boolean existsByNationalId(String nationalId);

    boolean existsByEmail(String email);

    List<Client> findByStatus(MemberStatus status);

    long countByStatus(MemberStatus status);

    List<Client> findByPolicy(Policy policy);

    Optional<Client> findByFullName(String fullName);

    List<Client> findByRoles_Name(RoleName roleName);

    Optional<Client> findByEmployeeId(String employeeId);

    Optional<Client> findByNationalId(String nationalId);

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

    /**
     * Find all healthcare providers by their roles or requestedRole
     */
    @Query("""
        SELECT DISTINCT c FROM Client c LEFT JOIN c.roles r
        WHERE r.name IN :roleNames
           OR (c.requestedRole IN :roleNames AND c.roleRequestStatus = 'APPROVED')
    """)
    List<Client> findAllHealthcareProviders(@Param("roleNames") List<RoleName> roleNames);

    /**
     * Find clients by specific role (including requestedRole if approved)
     */
    @Query("""
        SELECT DISTINCT c FROM Client c LEFT JOIN c.roles r
        WHERE r.name = :roleName
           OR (c.requestedRole = :roleName AND c.roleRequestStatus = 'APPROVED')
    """)
    List<Client> findByRoleOrRequestedRole(@Param("roleName") RoleName roleName);

    /**
     * Find all recipients for notifications (non-INSURANCE_CLIENT users)
     * Uses native query to avoid loading roles collection which may have invalid enum values
     */
    @Query(value = """
        SELECT DISTINCT c.id, c.full_name
        FROM clients c
        WHERE c.id NOT IN (
            SELECT cr.client_id FROM client_roles cr
            JOIN roles r ON cr.role_id = r.id
            WHERE r.name = 'INSURANCE_CLIENT'
        )
        AND c.full_name IS NOT NULL
    """, nativeQuery = true)
    List<Object[]> findAllRecipientsNative();

    /**
     * Debug: Get all users basic info using native query to avoid enum issues
     */
    @Query(value = """
        SELECT c.id, c.full_name, c.email, c.employee_id
        FROM clients c
    """, nativeQuery = true)
    List<Object[]> findAllUsersNative();

    /**
     * Debug: Get all insurance clients with their roles
     */
    @Query(value = """
        SELECT c.id, c.full_name, c.email, c.employee_id, r.name as role_name
        FROM clients c
        JOIN client_roles cr ON c.id = cr.client_id
        JOIN roles r ON cr.role_id = r.id
        WHERE r.name = 'INSURANCE_CLIENT'
    """, nativeQuery = true)
    List<Object[]> findAllInsuranceClientsNative();

    /**
     * Update employee ID for a specific client
     */
    @Modifying
    @Query("UPDATE Client c SET c.employeeId = :newEmployeeId WHERE c.id = :clientId")
    void updateEmployeeId(@Param("clientId") UUID clientId, @Param("newEmployeeId") String newEmployeeId);

    /**
     * Debug: Get all users with their roles
     */
    @Query(value = """
        SELECT c.id, c.full_name, c.email, r.name as role_name
        FROM clients c
        JOIN client_roles cr ON c.id = cr.client_id
        JOIN roles r ON cr.role_id = r.id
        ORDER BY c.full_name
    """, nativeQuery = true)
    List<Object[]> findAllUsersWithRolesNative();

    /**
     * Add a role to a client using native query
     */
    @Modifying
    @Query(value = """
        INSERT INTO client_roles (client_id, role_id)
        SELECT :clientId, r.id FROM roles r WHERE r.name = :roleName
        ON CONFLICT DO NOTHING
    """, nativeQuery = true)
    void addRoleToClient(@Param("clientId") UUID clientId, @Param("roleName") String roleName);

}

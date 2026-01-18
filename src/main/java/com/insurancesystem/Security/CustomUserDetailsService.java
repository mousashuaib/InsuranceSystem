package com.insurancesystem.Security;

import com.insurancesystem.Repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final ClientRepository clientRepo;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Use findByEmailWithRoles to ensure roles are properly fetched via JOIN FETCH
        var user = clientRepo.findByEmailWithRoles(email.toLowerCase())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        log.info("🔍 Loading user: {} | Roles from DB: {} | RequestedRole: {} | RoleRequestStatus: {}",
                email,
                user.getRoles() != null ? user.getRoles().size() : "null",
                user.getRequestedRole(),
                user.getRoleRequestStatus());

        // Get authorities from roles collection if available, otherwise use requestedRole
        var authorities = user.getRoles().stream()
                .map(r -> {
                    String authority = "ROLE_" + r.getName().name();
                    log.info("✅ Adding authority from roles collection: {}", authority);
                    return new SimpleGrantedAuthority(authority);
                })
                .toList();

        // If no roles in collection but has approved requestedRole, use that
        if (authorities.isEmpty() &&
            user.getRequestedRole() != null &&
            user.getRoleRequestStatus() == com.insurancesystem.Model.Entity.Enums.RoleRequestStatus.APPROVED) {
            log.info("⚠️ No roles in collection, using approved requestedRole: {}", user.getRequestedRole());
            authorities = java.util.List.of(
                new SimpleGrantedAuthority("ROLE_" + user.getRequestedRole().name())
            );
        }

        log.info("🎯 Final authorities for {}: {}", email, authorities);

        return User.withUsername(user.getEmail())
                .password(user.getPasswordHash())
                .authorities(authorities)
                .accountLocked(false)
                .disabled(false)
                .build();

    }
}
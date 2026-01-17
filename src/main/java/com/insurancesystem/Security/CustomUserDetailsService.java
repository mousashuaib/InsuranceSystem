package com.insurancesystem.Security;

import com.insurancesystem.Model.Entity.Enums.RoleRequestStatus;
import com.insurancesystem.Repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final ClientRepository clientRepo;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        var user = clientRepo.findByEmail(email.toLowerCase())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

<<<<<<< HEAD
        // Get authorities from roles
        List<SimpleGrantedAuthority> authorities = new ArrayList<>(
                user.getRoles().stream()
                        .map(r -> new SimpleGrantedAuthority("ROLE_" + r.getName().name()))
                        .toList()
        );

        // Fallback: if roles collection is empty but requestedRole is approved, use that
        if (authorities.isEmpty() &&
            user.getRequestedRole() != null &&
            user.getRoleRequestStatus() == RoleRequestStatus.APPROVED) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRequestedRole().name()));
        }

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
=======
        var authorities = user.getRoles().stream()
                .map(r -> new SimpleGrantedAuthority("ROLE_" + r.getName().name()))
                .toList();

        return User.withUsername(user.getEmail())
>>>>>>> 59fc73de7f549007a5658aab4146b5707a8a4bd8
                .password(user.getPasswordHash())
                .authorities(authorities)
                .accountLocked(false)
                .disabled(false)
                .build();
    }
<<<<<<< HEAD
}
=======
}
>>>>>>> 59fc73de7f549007a5658aab4146b5707a8a4bd8

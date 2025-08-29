package com.insurancesystem.Services;

import com.insurancesystem.Exception.NotFoundException;
import com.insurancesystem.Model.Dto.RoleDTO;
import com.insurancesystem.Model.Entity.Enums.RoleName;
import com.insurancesystem.Model.Entity.Role;
import com.insurancesystem.Model.MapStruct.RoleMapper;
import com.insurancesystem.Repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepo;
    private final RoleMapper roleMapper;

    public void seedIfMissing() {
        Arrays.stream(RoleName.values()).forEach(rn -> {
            if (!roleRepo.existsByName(rn)) {
                roleRepo.save(Role.builder().name(rn).build());
            }
        });
    }

    public Role getByNameOrThrow(RoleName name) {
        return roleRepo.findByName(name)
                .orElseThrow(() -> new NotFoundException("Role not found: " + name));
    }

    public List<RoleDTO> getAll() {
        return roleRepo.findAll().stream().map(roleMapper::toDTO).toList();
    }
}

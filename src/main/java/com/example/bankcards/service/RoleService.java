package com.example.bankcards.service;

import com.example.bankcards.entity.Role;
import com.example.bankcards.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;

    public Role getUserRole() {
        return roleRepository.findRoleByName("ROLE_USER").get();
    }
}

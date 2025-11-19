package com.example.bankcards.service;

import com.example.bankcards.entity.Role;
import com.example.bankcards.repository.RoleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoleServiceTest {

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private RoleService roleService;

    @Test
    void getUserRole_WhenRoleExists_ShouldReturnRole() {
        Role expectedRole = Role.builder()
                .id(1L)
                .name("ROLE_USER")
                .build();

        when(roleRepository.findRoleByName("ROLE_USER"))
                .thenReturn(Optional.of(expectedRole));

        Role actualRole = roleService.getUserRole();

        assertNotNull(actualRole);
        assertEquals(expectedRole.getId(), actualRole.getId());
        assertEquals(expectedRole.getName(), actualRole.getName());

        verify(roleRepository, times(1)).findRoleByName("ROLE_USER");
        verifyNoMoreInteractions(roleRepository);
    }

    @Test
    void getUserRole_WhenRoleDoesNotExist_ShouldThrowException() {
        when(roleRepository.findRoleByName("ROLE_USER"))
                .thenReturn(Optional.empty());

        assertThrows(java.util.NoSuchElementException.class, () -> roleService.getUserRole());

        verify(roleRepository, times(1)).findRoleByName("ROLE_USER");
    }

    @Test
    void getUserRole_ShouldCallRepositoryWithCorrectRoleName() {
        Role expectedRole = Role.builder()
                .id(2L)
                .name("ROLE_USER")
                .build();

        when(roleRepository.findRoleByName("ROLE_USER"))
                .thenReturn(Optional.of(expectedRole));

        roleService.getUserRole();

        verify(roleRepository).findRoleByName("ROLE_USER");
    }
}
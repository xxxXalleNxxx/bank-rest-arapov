package com.example.bankcards.service;

import com.example.bankcards.dto.UpdateUserRequest;
import com.example.bankcards.dto.UserDto;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.exception.UserOperationException;
import com.example.bankcards.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminUserManagementServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AdminUserManagementService adminUserManagementService;

    private User testUser;
    private Role userRole;
    private UpdateUserRequest updateUserRequest;

    @BeforeEach
    void setUp() {
        userRole = Role.builder()
                .id(1L)
                .name("ROLE_USER")
                .build();

        testUser = User.builder()
                .id(1L)
                .firstName("Arapov")
                .lastName("Artem")
                .email("arapov@gmail.com")
                .password("encodedPassword")
                .roles(Set.of(userRole))
                .build();

        updateUserRequest = new UpdateUserRequest(
                "Kazakov",
                "Timur",
                "kazakov@gmail.com"
        );
    }

    @Test
    void shouldGetAllReturnPageOfUsers() {
        Page<User> userPage = new PageImpl<>(List.of(testUser));
        when(userRepository.findAll(any(Pageable.class))).thenReturn(userPage);

        Page<UserDto> result = adminUserManagementService.getAllUsers(Pageable.ofSize(10));

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("Arapov", result.getContent().get(0).getFirstName());

        verify(userRepository).findAll(any(Pageable.class));
    }

    @Test
    void shouldGetAllReturnEmptyPage() {
        Page<User> emptyPage = Page.empty();
        when(userRepository.findAll(any(Pageable.class))).thenReturn(emptyPage);

        Page<UserDto> result = adminUserManagementService.getAllUsers(Pageable.ofSize(10));

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(userRepository).findAll(any(Pageable.class));
    }

    @Test
    void shouldGetByIdReturnUserDto() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        UserDto result = adminUserManagementService.getUserById(1L);

        assertNotNull(result);
        assertEquals("Arapov", result.getFirstName());
        assertEquals("Artem", result.getLastName());
        assertEquals("arapov@gmail.com", result.getEmail());

        verify(userRepository).findById(1L);
    }

    @Test
    void shouldThrowUserNotFoundException() {
        when(userRepository.findById(52L)).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> adminUserManagementService.getUserById(52L));

        assertEquals("User not found: 52", exception.getMessage());
        verify(userRepository).findById(52L);
    }

    @Test
    void shouldUpdateOnlyProvidedFields() {
        UpdateUserRequest partialRequest = new UpdateUserRequest(
                "Kazakov",
                null,
                null
        );

        User updatedUser = User.builder()
                .id(1L)
                .firstName("ZVO")
                .lastName("ZVO")
                .email("zvo@gmail.com")
                .password("encodedPassword")
                .roles(List.of(userRole))
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        UserDto result = adminUserManagementService.updateUser(1L, partialRequest);

        assertNotNull(result);
        assertEquals("ZVO", result.getFirstName());
        assertEquals("ZVO", result.getLastName());
        assertEquals("zvo@gmail.com", result.getEmail());

        verify(userRepository).findById(1L);
        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldUpdateThrowUserOperationException() {
        UpdateUserRequest requestWithExistingEmail = new UpdateUserRequest(
                "Kazakov",
                "Timur",
                "kazakov@gmail.com"
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmail("kazakov@gmail.com")).thenReturn(true);

        UserOperationException exception = assertThrows(UserOperationException.class,
                () -> adminUserManagementService.updateUser(1L, requestWithExistingEmail));

        assertEquals("Пользователь с такой почтой kazakov@gmail.com уже существует", exception.getMessage());

        verify(userRepository).findById(1L);
        verify(userRepository).existsByEmail("kazakov@gmail.com");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldUpdateThrowUserNotFoundException() {
        when(userRepository.findById(52L)).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> adminUserManagementService.updateUser(52L, updateUserRequest));

        assertEquals("User not found: 52", exception.getMessage());
        verify(userRepository).findById(52L);
        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldDeleteThrowUserNotFoundException() {
        when(userRepository.findById(52L)).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> adminUserManagementService.deleteVoid(52L));

        assertEquals("User not found: 52", exception.getMessage());
        verify(userRepository).findById(52L);
        verify(userRepository, never()).delete(any(User.class));
    }
}
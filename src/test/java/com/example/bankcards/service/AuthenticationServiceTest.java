package com.example.bankcards.service;

import com.example.bankcards.dto.*;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.RoleNotFoundException;
import com.example.bankcards.exception.UserOperationException;
import com.example.bankcards.repository.RoleRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.JwtTokenUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Optional;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenUtil jwtTokenUtil;

    @InjectMocks
    private AuthenticationService authenticationService;

    private User testUser;
    private CreateUserRequest createUserRequest;
    private JwtRequest jwtRequest;

    @BeforeEach
    void setUp() {
        Role userRole = Role.builder()
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

        createUserRequest = new CreateUserRequest(
                "Unguryan",
                "Sergey",
                "unguryan@gmail.com",
                "password123"
        );

        jwtRequest = new JwtRequest(
                "unguryan@gmail.com",
                "password123"
        );
    }

    @Test
    void shouldThrowRoleNotFoundException() {
        when(userRepository.existsByEmail("unguryan@gmail.com")).thenReturn(false);
        when(roleRepository.findRoleByName("ROLE_USER")).thenReturn(Optional.empty());

        RoleNotFoundException exception = assertThrows(RoleNotFoundException.class,
                () -> authenticationService.register(createUserRequest));

        assertEquals("ROLE_USER not found", exception.getMessage());
        verify(userRepository).existsByEmail("unguryan@gmail.com");
        verify(roleRepository).findRoleByName("ROLE_USER");
        verifyNoInteractions(passwordEncoder, jwtTokenUtil);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldReturnJwtResponse() {
        when(userRepository.findUserByEmail("unguryan@gmail.com"))
                .thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);
        when(jwtTokenUtil.generateToken(testUser)).thenReturn("jwt-token");

        JwtResponse response = authenticationService.login(jwtRequest);

        assertNotNull(response);
        assertEquals("jwt-token", response.token());

        verify(userRepository).findUserByEmail("unguryan@gmail.com");
        verify(passwordEncoder).matches("password123", "encodedPassword");
        verify(jwtTokenUtil).generateToken(testUser);
    }

    @Test
    void shouldThrowUserOperationException() {
        when(userRepository.findUserByEmail("unguryan@gmail.com"))
                .thenReturn(Optional.empty());

        UserOperationException exception = assertThrows(UserOperationException.class,
                () -> authenticationService.login(jwtRequest));

        assertEquals("User with emailunguryan@gmail.comalready exists", exception.getMessage());
        verify(userRepository).findUserByEmail("unguryan@gmail.com");
        verifyNoInteractions(passwordEncoder, jwtTokenUtil);
    }

    @Test
    void shouldThrowBadCredentialsException() {
        when(userRepository.findUserByEmail("unguryan@gmail.com"))
                .thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

        JwtRequest invalidRequest = new JwtRequest("unguryan@gmail.com", "wrongPassword");

        BadCredentialsException exception = assertThrows(BadCredentialsException.class,
                () -> authenticationService.login(invalidRequest));

        assertEquals("Invalid email or password", exception.getMessage());
        verify(userRepository).findUserByEmail("unguryan@gmail.com");
        verify(passwordEncoder).matches("wrongPassword", "encodedPassword");
        verifyNoInteractions(jwtTokenUtil);
    }

    @Test
    void shouldReturnValidResponse() {
        String token = "valid-jwt-token";

        when(jwtTokenUtil.validateToken(token)).thenReturn(true);
        when(jwtTokenUtil.getUsername(token)).thenReturn("unguryan@gmail.com");
        when(userRepository.findUserByEmail("unguryan@gmail.com"))
                .thenReturn(Optional.of(testUser));

        ValidateTokenResponse response = authenticationService.validateToken(token);

        assertNotNull(response);
        assertEquals("Token is valid", response.message());
        assertNotNull(response.user());

        verify(jwtTokenUtil).validateToken(token);
        verify(jwtTokenUtil).getUsername(token);
        verify(userRepository).findUserByEmail("unguryan@gmail.com");
    }

    @Test
    void shouldReturnInvalidResponse() {
        String token = "invalid-jwt-token";

        when(jwtTokenUtil.validateToken(token)).thenReturn(false);

        ValidateTokenResponse response = authenticationService.validateToken(token);

        assertNotNull(response);
        assertEquals("Token is invalid", response.message());
        assertNull(response.user());

        verify(jwtTokenUtil).validateToken(token);
        verify(jwtTokenUtil, never()).getUsername(anyString());
        verifyNoInteractions(userRepository);
    }

    @Test
    void shouldReturnInvalidResponseWhenTokenInvalid() {
        String token = "invalid-jwt-token";

        when(jwtTokenUtil.validateToken(token)).thenThrow(new RuntimeException("Token expired"));

        ValidateTokenResponse response = authenticationService.validateToken(token);

        assertNotNull(response);
        assertTrue(response.message().contains("Token validation failed"));
        assertNull(response.user());

        verify(jwtTokenUtil).validateToken(token);
    }

    @Test
    void shouldReturnInvalidResponseWhenUserNotFound() {
        String token = "valid-jwt-token";

        when(jwtTokenUtil.validateToken(token)).thenReturn(true);
        when(jwtTokenUtil.getUsername(token)).thenReturn("nonexistent@example.com");
        when(userRepository.findUserByEmail("nonexistent@example.com"))
                .thenReturn(Optional.empty());

        ValidateTokenResponse response = authenticationService.validateToken(token);

        assertNotNull(response);
        assertTrue(response.message().contains("Token validation failed"));
        assertNull(response.user());

        verify(jwtTokenUtil).validateToken(token);
        verify(jwtTokenUtil).getUsername(token);
        verify(userRepository).findUserByEmail("nonexistent@example.com");
    }
}
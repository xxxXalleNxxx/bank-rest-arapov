package com.example.bankcards.service;

import com.example.bankcards.dto.*;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.RoleNotFoundException;
import com.example.bankcards.exception.UserOperationException;
import com.example.bankcards.repository.RoleRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.JwtTokenUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenUtil jwtTokenUtil;

    public JwtResponse register(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new UserOperationException("User with email" + request.email() + "already exists");
        }

        Role userRole = roleRepository.findRoleByName("ROLE_USER")
                .orElseThrow(() -> new RoleNotFoundException("ROLE_USER not found"));

        User user = User.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .roles(Set.of(userRole))
                .build();

        User savedUser = userRepository.save(user);
        String token = jwtTokenUtil.generateToken(savedUser);
        return new JwtResponse(token);
    }

    public JwtResponse login(JwtRequest request) {
        User user = userRepository.findUserByEmail(request.email())
                .orElseThrow(() -> new UserOperationException("User with email" + request.email() + "already exists"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        if (!user.isEnabled()) {
            throw new DisabledException("User account is disabled");
        }

        String token = jwtTokenUtil.generateToken(user);
        return new JwtResponse(token);
    }

    public ValidateTokenResponse validateToken(String token) {

        try {
            if (jwtTokenUtil.validateToken(token)) {
                String username = jwtTokenUtil.getUsername(token);
                User user = userRepository.findUserByEmail(username)
                        .orElseThrow(() -> new UsernameNotFoundException("User not found"));

                return new ValidateTokenResponse(true, "Token is valid", UserDto.from(user));
            } else {
                return new ValidateTokenResponse(false, "Token is invalid", null);
            }
        } catch (Exception e) {
            return new ValidateTokenResponse(false, "Token validation failed: " + e.getMessage(), null);
        }
    }
}

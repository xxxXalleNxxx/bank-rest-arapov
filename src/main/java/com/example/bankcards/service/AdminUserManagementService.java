package com.example.bankcards.service;

import com.example.bankcards.dto.CreateUserRequest;
import com.example.bankcards.dto.UpdateUserRequest;
import com.example.bankcards.dto.UserDto;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.exception.UserOperationException;
import com.example.bankcards.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminUserManagementService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleService roleService;

    public UserDto createUser(CreateUserRequest createUserRequest) {
        User user = User.builder()
                .firstName(createUserRequest.firstName())
                .lastName(createUserRequest.lastName())
                .email(createUserRequest.email())
                .password(passwordEncoder.encode(createUserRequest.password()))
                .roles(List.of(roleService.getUserRole()))
                .build();

        User savedUser = userRepository.save(user);
        return UserDto.from(savedUser);
    }

    public Page<UserDto> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(UserDto::from);
    }

    public UserDto getUserById(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("User not found: " + userId));
        return UserDto.from(user);
    }

    public UserDto updateUser(Long userId, UpdateUserRequest request) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("User not found: " + userId));

        if (request.firstname() != null) {
            user.setFirstName(request.firstname());
        }
        if (request.lastName() != null) {
            user.setLastName(request.lastName());
        }

        if (request.email() != null && !request.email().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.email())) {
                throw new UserOperationException("Пользователь с такой почтой " + request.email() + " уже существует");
            }

            user.setEmail(request.email());
        }
        User updatedUser = userRepository.save(user);
        return UserDto.from(updatedUser);
    }

    public void deleteVoid(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("User not found: " + userId));

        if (user.getCards().stream().noneMatch(card -> card.getBalance().compareTo(BigDecimal.ZERO) > 0)) {
            throw new UserOperationException("Невозможно удалить пользователя с положительным балансом на картах");
        }

        userRepository.delete(user);


    }
}

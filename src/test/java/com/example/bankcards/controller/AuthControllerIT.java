package com.example.bankcards.controller;

import com.example.bankcards.dto.CreateUserRequest;
import com.example.bankcards.dto.JwtRequest;
import com.example.bankcards.dto.JwtResponse;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@AutoConfigureMockMvc
public class AuthControllerIT {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void shouldRegisterUser() {
        CreateUserRequest request = new CreateUserRequest("Arapov", "Artem", "arapov@gmail.com", "password123");

        ResponseEntity<JwtResponse> response = restTemplate.postForEntity(
                "/api/v1/auth/register", request, JwtResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().token()).isNotBlank();

        Optional<User> savedUser = userRepository.findUserByEmail("arapov@gmail.com");
        assertThat(savedUser).isPresent();
        assertThat(passwordEncoder.matches("password123", savedUser.get().getPassword())).isTrue();
    }

    @Test
    void shouldLoginReturnToken() {
        User user = User.builder()
                .firstName("Arapov")
                .lastName("Artem")
                .email("arapov@gmail.com")
                .password(passwordEncoder.encode("password123"))
                .build();
        userRepository.save(user);


        JwtRequest request = new JwtRequest("arapov@gmail.com", "password123");

        ResponseEntity<JwtResponse> response = restTemplate.postForEntity(
                "/api/v1/auth/login", request, JwtResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().token()).isNotBlank();
    }
}
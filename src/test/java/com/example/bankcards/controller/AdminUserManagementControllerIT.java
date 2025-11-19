package com.example.bankcards.controller;

import com.example.bankcards.dto.CreateUserRequest;
import com.example.bankcards.dto.UserDto;
import com.example.bankcards.service.AdminUserManagementService;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@AutoConfigureMockMvc
class AdminUserManagementControllerIT {

    @Autowired
    private TestRestTemplate restTemplate;

    @Mock
    private AdminUserManagementService adminUserManagementService;

    @Test
    void shouldReturnCreatedUser() {
        CreateUserRequest request = new CreateUserRequest("Arapov", "Artem", "arapov@gmail.com", "password123");
        UserDto userDto = new UserDto(1L, "Arapov", "Artem", "arapov@gmail.com", 0, BigDecimal.ZERO);

        when(adminUserManagementService.createUser(any(CreateUserRequest.class))).thenReturn(userDto);

        HttpEntity<CreateUserRequest> entity = new HttpEntity<>(request);

        ResponseEntity<UserDto> response = restTemplate.postForEntity(
                "/api/v1/admin/users",
                entity,
                UserDto.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        if (response.getBody() != null) {
            assertThat(response.getBody().getId()).isEqualTo(1L);
            assertThat(response.getBody().getFirstName()).isEqualTo("Arapov");
        }
    }

    @Test
    void shouldReturnDeleteStatus() {
        Long userId = 1L;

        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/v1/admin/users/{userId}",
                HttpMethod.DELETE,
                null,
                Void.class,
                userId
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void shouldReturnAllUsers() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/v1/admin/users?page=0&size=10",
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
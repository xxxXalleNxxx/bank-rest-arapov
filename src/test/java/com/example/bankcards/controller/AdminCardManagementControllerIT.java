package com.example.bankcards.controller;

import com.example.bankcards.service.AdminCardManagementService;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@AutoConfigureMockMvc
class AdminCardManagementControllerIT {

    @Autowired
    private TestRestTemplate restTemplate;

    @Mock
    private AdminCardManagementService adminCardManagementService;

    @Test
    void shouldReturnBlockStatusOk() {
        Long cardId = 1L;

        ResponseEntity<Void> response = restTemplate.postForEntity(
                "/api/v1/admin/cards/{cardId}/block",
                null,
                Void.class,
                cardId
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void shouldReturnActivateStatusOk() {
        Long cardId = 1L;

        ResponseEntity<Void> response = restTemplate.postForEntity(
                "/api/v1/admin/cards/{cardId}/activate",
                null,
                Void.class,
                cardId
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void shouldReturnDeleteOk() {
        Long cardId = 1L;

        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/v1/admin/cards/{cardId}",
                HttpMethod.DELETE,
                null,
                Void.class,
                cardId
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
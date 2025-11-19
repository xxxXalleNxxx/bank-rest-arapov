package com.example.bankcards.controller;

import com.example.bankcards.dto.JwtRequest;
import com.example.bankcards.dto.JwtResponse;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.http.*;
import org.springframework.boot.test.web.client.TestRestTemplate;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import static org.assertj.core.api.Assertions.assertThat;
import java.math.BigDecimal;
import java.security.Key;
import java.time.LocalDate;
import java.util.Base64;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@AutoConfigureMockMvc
public class CardControllerIT {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        cardRepository.deleteAll();
        userRepository.deleteAll();

        getOrCreateTestUser();
    }

    private User getOrCreateTestUser() {
        return userRepository.findUserByEmail("arapov@gmail.com")
                .orElseGet(() -> {
                    User user = User.builder()
                            .firstName("Arapov")
                            .lastName("Artem")
                            .email("arapov@gmail.com")
                            .password(passwordEncoder.encode("password123"))
                            .build();
                    return userRepository.save(user);
                });
    }

    private String encryptWithAES(String data) {
        try {
            String secretKey = "arapov-key-aes16";

            Key key = new SecretKeySpec(secretKey.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] encrypted = cipher.doFinal(data.getBytes());
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("AES encryption failed", e);
        }
    }

    private Card createTestCard(User user, BigDecimal balance) {
        String cardNumber = "1234567890123456";
        String encryptedCardNumber = encryptWithAES(cardNumber);

        Card card = Card.builder()
                .cardNumber(encryptedCardNumber)
                .lastFourNumbers("3456")
                .expiryDate(LocalDate.now().plusYears(2))
                .balance(balance)
                .owner(user)
                .status(CardStatus.ACTIVE)
                .build();
        return cardRepository.save(card);
    }

    private String getAuthToken() {
        JwtRequest request = new JwtRequest("arapov@gmail.com", "password123");
        ResponseEntity<JwtResponse> response = restTemplate.postForEntity(
                "/api/v1/auth/login", request, JwtResponse.class);

        return response.getBody().token();
    }

    @Test
    void getUserCards_ShouldReturnPageOfCards() {
        String token = getAuthToken();
        User user = getOrCreateTestUser();
        createTestCard(user, BigDecimal.valueOf(1000));

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/v1/cards",
                HttpMethod.GET,
                entity,
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("content");
    }

    @Test
    void shouldReturnBalance() {
        String token = getAuthToken();
        User user = getOrCreateTestUser();
        Card card = createTestCard(user, BigDecimal.valueOf(1500));

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<BigDecimal> response = restTemplate.exchange(
                "/api/v1/cards/" + card.getId() + "/balance",
                HttpMethod.GET,
                entity,
                BigDecimal.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualByComparingTo("1500");
    }

    @Test
    void shouldReturnBlockCardRequest() {
        String token = getAuthToken();
        User user = getOrCreateTestUser();
        Card card = createTestCard(user, BigDecimal.valueOf(1000));

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/v1/cards/" + card.getId() + "/request-block?reason=потерялась",
                HttpMethod.POST,
                entity,
                Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void shouldReturnAuthorizedAccess() {

        ResponseEntity<String> cardsResponse = restTemplate.getForEntity("/api/v1/cards", String.class);
        assertThat(cardsResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<String> balanceResponse = restTemplate.getForEntity("/api/v1/cards/1/balance", String.class);
        assertThat(balanceResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}

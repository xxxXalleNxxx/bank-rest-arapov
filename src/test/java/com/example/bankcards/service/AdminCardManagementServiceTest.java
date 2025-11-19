package com.example.bankcards.service;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.CreateCardRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.exception.CardOperationException;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.repository.BlockCardRequestRepository;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.TransactionRepository;
import com.example.bankcards.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminCardManagementServiceTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private BlockCardRequestRepository blockCardRequestRepository;

    @InjectMocks
    private AdminCardManagementService adminCardManagementService;

    private User testUser;
    private Card activeCard;
    private Card expiredCard;
    private Card blockedCard;
    private CreateCardRequest createCardRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .firstName("Arapov")
                .lastName("Artem")
                .email("arapov@gmail.com")
                .password("encodedPassword")
                .roles(Set.of())
                .build();

        activeCard = Card.builder()
                .id(1L)
                .cardNumber("encrypted1234567890123456")
                .lastFourNumbers("3456")
                .expiryDate(LocalDate.now().plusYears(2))
                .balance(BigDecimal.ZERO)
                .status(CardStatus.ACTIVE)
                .owner(testUser)
                .build();

        expiredCard = Card.builder()
                .id(2L)
                .cardNumber("encrypted9876543210987654")
                .lastFourNumbers("7654")
                .expiryDate(LocalDate.now().minusMonths(1))
                .balance(BigDecimal.ZERO)
                .status(CardStatus.ACTIVE)
                .owner(testUser)
                .build();

        blockedCard = Card.builder()
                .id(3L)
                .cardNumber("encrypted1111222233334444")
                .lastFourNumbers("4444")
                .expiryDate(LocalDate.now().plusYears(1))
                .balance(BigDecimal.ZERO)
                .status(CardStatus.BLOCKED)
                .owner(testUser)
                .build();

        createCardRequest = new CreateCardRequest();
        createCardRequest.setOwnerId(1L);
        createCardRequest.setCardNumber("1234567890123456");
        createCardRequest.setExpiryDate(LocalDate.now().plusYears(3));
        createCardRequest.setInitialBalance(BigDecimal.valueOf(1000));
    }

    @Test
    void shouldCreateCardSuccessfully() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> {
            Card card = invocation.getArgument(0);
            card.setId(1L);
            return card;
        });

        CardDto result = adminCardManagementService.createCard(createCardRequest);

        assertNotNull(result);
        assertEquals(LocalDate.now().plusYears(3), result.expiryDate());
        assertEquals(BigDecimal.valueOf(1000), result.balance());

        verify(userRepository).findById(1L);
        verify(cardRepository).save(any(Card.class));
    }

    @Test
    void shouldThrowUserNotFoundExceptionWhenCreate() {
        when(userRepository.findById(52L)).thenReturn(Optional.empty());

        CreateCardRequest request = new CreateCardRequest();
        request.setOwnerId(52L);
        request.setCardNumber("1234567890123456");
        request.setExpiryDate(LocalDate.now().plusYears(1));

        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> adminCardManagementService.createCard(request));

        assertEquals("Пользователь не найден", exception.getMessage());
        verify(userRepository).findById(52L);
        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    void shouldUseZeroBalanceWhenCreate() {
        CreateCardRequest requestWithoutBalance = new CreateCardRequest();
        requestWithoutBalance.setOwnerId(1L);
        requestWithoutBalance.setCardNumber("1234567890123456");
        requestWithoutBalance.setExpiryDate(LocalDate.now().plusYears(1));
        requestWithoutBalance.setInitialBalance(null);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> {
            Card card = invocation.getArgument(0);
            card.setId(1L);
            return card;
        });

        CardDto result = adminCardManagementService.createCard(requestWithoutBalance);

        assertNotNull(result);
        verify(userRepository).findById(1L);
        verify(cardRepository).save(any(Card.class));
    }

    @Test
    void shouldReturnEmptyPageWhenNoCards() {
        Page<Card> emptyPage = Page.empty();
        when(cardRepository.findAll(any(Pageable.class))).thenReturn(emptyPage);

        Page<CardDto> result = adminCardManagementService.getAllCards(Pageable.ofSize(10));

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(cardRepository).findAll(any(Pageable.class));
    }

    @Test
    void shouldThrowCardNotFoundExceptionWhenDelete() {
        when(cardRepository.findById(52L)).thenReturn(Optional.empty());

        CardNotFoundException exception = assertThrows(CardNotFoundException.class,
                () -> adminCardManagementService.deleteCard(52L));

        assertEquals("Карта не найдена", exception.getMessage());

        verify(cardRepository).findById(52L);
        verify(transactionRepository, never()).setFromCardToNull(anyLong());
        verify(transactionRepository, never()).setToCardToNull(anyLong());
        verify(blockCardRequestRepository, never()).setCardToNull(anyLong());
        verify(cardRepository, never()).delete(any(Card.class));
    }

    @Test
    void shouldActivateCardSuccessfully() {
        when(cardRepository.findById(1L)).thenReturn(Optional.of(activeCard));
        when(cardRepository.save(any(Card.class))).thenReturn(activeCard);

        adminCardManagementService.activateCard(1L);

        assertEquals(CardStatus.ACTIVE, activeCard.getStatus());
        verify(cardRepository).findById(1L);
        verify(cardRepository).save(activeCard);
    }

    @Test
    void shouldThrowCardOperationExceptionWhenActivated() {
        when(cardRepository.findById(2L)).thenReturn(Optional.of(expiredCard));

        CardOperationException exception = assertThrows(CardOperationException.class,
                () -> adminCardManagementService.activateCard(2L));

        assertEquals("Карта просрочена", exception.getMessage());

        verify(cardRepository).findById(2L);
        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    void shouldThrowCardNotFoundExceptionWhenActivated() {
        when(cardRepository.findById(52L)).thenReturn(Optional.empty());

        CardNotFoundException exception = assertThrows(CardNotFoundException.class,
                () -> adminCardManagementService.activateCard(52L));

        assertEquals("Карта не найдена", exception.getMessage());

        verify(cardRepository).findById(52L);
        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    void shouldBlockCardSuccessfully() {
        when(cardRepository.findById(1L)).thenReturn(Optional.of(activeCard));
        when(cardRepository.save(any(Card.class))).thenReturn(activeCard);

        adminCardManagementService.blockCard(1L);

        assertEquals(CardStatus.BLOCKED, activeCard.getStatus());
        verify(cardRepository).findById(1L);
        verify(cardRepository).save(activeCard);
    }

    @Test
    void shouldThrowCardOperationException() {
        Card cardWithNegativeBalance = Card.builder()
                .id(1L)
                .balance(BigDecimal.valueOf(-25))
                .status(CardStatus.ACTIVE)
                .build();

        when(cardRepository.findById(1L)).thenReturn(Optional.of(cardWithNegativeBalance));

        CardOperationException exception = assertThrows(CardOperationException.class,
                () -> adminCardManagementService.blockCard(1L));

        assertEquals("Баланс карты должен быть нулевым", exception.getMessage());

        verify(cardRepository).findById(1L);
        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    void shouldThrowCardNotFoundExceptionWhen() {
        when(cardRepository.findById(52L)).thenReturn(Optional.empty());

        CardNotFoundException exception = assertThrows(CardNotFoundException.class,
                () -> adminCardManagementService.blockCard(52L));

        assertEquals("Карта не найдена", exception.getMessage());

        verify(cardRepository).findById(52L);
        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    void shouldUpdateStatusWhenAlreadyBlocked() {
        when(cardRepository.findById(3L)).thenReturn(Optional.of(blockedCard));
        when(cardRepository.save(any(Card.class))).thenReturn(blockedCard);

        adminCardManagementService.blockCard(3L);

        assertEquals(CardStatus.BLOCKED, blockedCard.getStatus());
        verify(cardRepository).findById(3L);
        verify(cardRepository).save(blockedCard);
    }
}
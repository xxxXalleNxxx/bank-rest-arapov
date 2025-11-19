package com.example.bankcards.service;

import com.example.bankcards.dto.CardSearchRequest;
import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.entity.*;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.exception.CardOperationException;
import com.example.bankcards.exception.InsufficientException;
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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CardRepository cardRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private BlockCardRequestRepository blockCardRequestRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private CardService cardService;

    private User testUser;
    private Card activeCard1;
    private Card activeCard2;
    private Card blockedCard;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("arapov@gmail.com")
                .build();

        activeCard1 = Card.builder()
                .id(1L)
                .cardNumber("encrypted1234567890123456")
                .expiryDate(LocalDate.now().plusYears(2))
                .balance(BigDecimal.valueOf(1000))
                .owner(testUser)
                .status(CardStatus.ACTIVE)
                .build();

        activeCard2 = Card.builder()
                .id(2L)
                .cardNumber("encrypted9876543210987654")
                .expiryDate(LocalDate.now().plusYears(1))
                .balance(BigDecimal.valueOf(500))
                .owner(testUser)
                .status(CardStatus.ACTIVE)
                .build();

        blockedCard = Card.builder()
                .id(3L)
                .cardNumber("encrypted1111222233334444")
                .expiryDate(LocalDate.now().plusYears(3))
                .balance(BigDecimal.valueOf(200))
                .owner(testUser)
                .status(CardStatus.BLOCKED)
                .build();

        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("arapov@gmail.com");
    }

    @Test
    void shouldThrowException() {
        CardSearchRequest request = new CardSearchRequest();

        when(userRepository.findUserByEmail("arapov@gmail.com"))
                .thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> cardService.getUserCards(request));
        verify(userRepository).findUserByEmail("arapov@gmail.com");
        verifyNoInteractions(cardRepository);
    }

    @Test
    void shouldTransferSuccessfully() {
        TransferRequest request = new TransferRequest();
        request.setFromCardId(1L);
        request.setToCardId(2L);
        request.setAmount(BigDecimal.valueOf(100));
        request.setDescription("Test transfer");

        when(userRepository.findUserByEmail("arapov@gmail.com"))
                .thenReturn(Optional.of(testUser));
        when(cardRepository.existsByIdAndOwnerId(1L, 1L)).thenReturn(true);
        when(cardRepository.existsByIdAndOwnerId(2L, 1L)).thenReturn(true);
        when(cardRepository.findById(1L)).thenReturn(Optional.of(activeCard1));
        when(cardRepository.findById(2L)).thenReturn(Optional.of(activeCard2));
        when(cardRepository.saveAll(anyList())).thenReturn(List.of(activeCard1, activeCard2));
        when(transactionRepository.save(any(Transactions.class))).thenReturn(null);

        cardService.transferBetweenCards(request);

        assertEquals(BigDecimal.valueOf(900), activeCard1.getBalance());
        assertEquals(BigDecimal.valueOf(600), activeCard2.getBalance());
        verify(cardRepository).saveAll(anyList());
        verify(transactionRepository).save(any(Transactions.class));
    }

    @Test
    void shouldThrowAccessDeniedException() {
        TransferRequest request = new TransferRequest();
        request.setFromCardId(1L);
        request.setToCardId(2L);
        request.setAmount(BigDecimal.valueOf(100));

        when(userRepository.findUserByEmail("arapov@gmail.com"))
                .thenReturn(Optional.of(testUser));
        when(cardRepository.existsByIdAndOwnerId(1L, 1L)).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> cardService.transferBetweenCards(request));
        verify(cardRepository, never()).findById(anyLong());
    }

    @Test
    void shouldThrowInsufficientException() {
        TransferRequest request = new TransferRequest();
        request.setFromCardId(1L);
        request.setToCardId(2L);
        request.setAmount(BigDecimal.valueOf(2000));

        when(userRepository.findUserByEmail("arapov@gmail.com"))
                .thenReturn(Optional.of(testUser));
        when(cardRepository.existsByIdAndOwnerId(1L, 1L)).thenReturn(true);
        when(cardRepository.existsByIdAndOwnerId(2L, 1L)).thenReturn(true);
        when(cardRepository.findById(1L)).thenReturn(Optional.of(activeCard1));
        when(cardRepository.findById(2L)).thenReturn(Optional.of(activeCard2));

        assertThrows(InsufficientException.class, () -> cardService.transferBetweenCards(request));
        verify(cardRepository, never()).saveAll(anyList());
    }

    @Test
    void shouldUseDefaultDescription() {
        TransferRequest request = new TransferRequest();
        request.setFromCardId(1L);
        request.setToCardId(2L);
        request.setAmount(BigDecimal.valueOf(100));
        request.setDescription(null);

        when(userRepository.findUserByEmail("arapov@gmail.com"))
                .thenReturn(Optional.of(testUser));
        when(cardRepository.existsByIdAndOwnerId(1L, 1L)).thenReturn(true);
        when(cardRepository.existsByIdAndOwnerId(2L, 1L)).thenReturn(true);
        when(cardRepository.findById(1L)).thenReturn(Optional.of(activeCard1));
        when(cardRepository.findById(2L)).thenReturn(Optional.of(activeCard2));
        when(cardRepository.saveAll(anyList())).thenReturn(List.of(activeCard1, activeCard2));
        when(transactionRepository.save(any(Transactions.class))).thenAnswer(invocation -> {
            Transactions transaction = invocation.getArgument(0);
            assertEquals("Перевод между картами", transaction.getDescription());
            return transaction;
        });

        cardService.transferBetweenCards(request);

        verify(transactionRepository).save(any(Transactions.class));
    }

    @Test
    void shouldReturnBalance() {
        when(userRepository.findUserByEmail("arapov@gmail.com"))
                .thenReturn(Optional.of(testUser));
        when(cardRepository.findById(1L)).thenReturn(Optional.of(activeCard1));

        BigDecimal balance = cardService.getCardBalance(1L);

        assertEquals(BigDecimal.valueOf(1000), balance);
        verify(cardRepository).findById(1L);
    }

    @Test
    void shouldThrowCardNotFoundException() {
        when(userRepository.findUserByEmail("arapov@gmail.com"))
                .thenReturn(Optional.of(testUser));
        when(cardRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(CardNotFoundException.class, () -> cardService.getCardBalance(999L));
    }

    @Test
    void shouldCreateBlockRequest() {
        String reason = "Lost card";

        when(userRepository.findUserByEmail("arapov@gmail.com"))
                .thenReturn(Optional.of(testUser));
        when(cardRepository.findById(1L)).thenReturn(Optional.of(activeCard1));
        when(blockCardRequestRepository.save(any(BlockCardRequest.class))).thenReturn(null);

        cardService.requestCardBlock(1L, reason);

        verify(blockCardRequestRepository).save(any(BlockCardRequest.class));
    }

    @Test
    void shouldThrowCardOperationException() {
        String reason = "Lost card";

        when(userRepository.findUserByEmail("arapov@gmail.com"))
                .thenReturn(Optional.of(testUser));
        when(cardRepository.findById(3L)).thenReturn(Optional.of(blockedCard));

        assertThrows(CardOperationException.class, () -> cardService.requestCardBlock(3L, reason));
        verify(blockCardRequestRepository, never()).save(any(BlockCardRequest.class));
    }

    @Test
    void shouldCreateRequestWithNullReason() {
        when(userRepository.findUserByEmail("arapov@gmail.com"))
                .thenReturn(Optional.of(testUser));
        when(cardRepository.findById(1L)).thenReturn(Optional.of(activeCard1));
        when(blockCardRequestRepository.save(any(BlockCardRequest.class))).thenAnswer(invocation -> {
            BlockCardRequest request = invocation.getArgument(0);
            assertNull(request.getReason());
            return request;
        });

        cardService.requestCardBlock(1L, null);

        verify(blockCardRequestRepository).save(any(BlockCardRequest.class));
    }
}
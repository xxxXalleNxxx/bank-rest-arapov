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
import com.example.bankcards.util.CardEncryptor;
import com.example.bankcards.util.CardMasker;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminCardManagementService {

    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final BlockCardRequestRepository blockCardRequestRepository;

    public CardDto createCard(CreateCardRequest request) {
        User owner = userRepository.findById(request.getOwnerId()).orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));

        Card card = Card.builder()
                .owner(owner)
                .cardNumber(CardEncryptor.encrypt(request.getCardNumber()))
                .lastFourNumbers(CardMasker.getLastNums(request.getCardNumber()))
                .expiryDate(request.getExpiryDate())
                .status(CardStatus.ACTIVE)
                .balance(request.getInitialBalance() != null ? request.getInitialBalance() : BigDecimal.ZERO)
                .build();

        Card savedCard = cardRepository.save(card);
        return CardDto.from(savedCard);
    }

    public Page<CardDto> getAllCards(Pageable pageable) {
        return cardRepository.findAll(pageable)
                .map(CardDto::from);
    }

    public void deleteCard(Long cardId) {
        Card card = cardRepository.findById(cardId).orElseThrow(() -> new CardNotFoundException("Карта не найдена"));

        if (card.getBalance().compareTo(BigDecimal.ZERO) != 0) {
            throw new CardOperationException("Баланс карты должен быть нулевым");
        }

        transactionRepository.setFromCardToNull(cardId);
        transactionRepository.setToCardToNull(cardId);
        blockCardRequestRepository.setCardToNull(cardId);

        cardRepository.delete(card);
    }

    public void activateCard(Long cardId) {
        Card card = cardRepository.findById(cardId).orElseThrow(() -> new CardNotFoundException("Карта не найдена"));

        if (card.isExpired()) {
            throw new CardOperationException("Карта просрочена");
        }

        card.setStatus(CardStatus.ACTIVE);
        cardRepository.save(card);
    }

    public void blockCard(Long cardId) {
        Card card = cardRepository.findById(cardId).orElseThrow(() -> new CardNotFoundException("Карта не найдена"));

        if (card.getBalance().compareTo(BigDecimal.ZERO) != 0) {
            throw new CardOperationException("Баланс карты должен быть нулевым");
        }

        card.setStatus(CardStatus.BLOCKED);
        cardRepository.save(card);
    }
}

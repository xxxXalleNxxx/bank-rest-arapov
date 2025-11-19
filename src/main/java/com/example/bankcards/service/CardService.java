package com.example.bankcards.service;

import com.example.bankcards.dto.CardDto;
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
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CardService {

    private final UserRepository userRepository;
    private final CardRepository cardRepository;
    private final TransactionRepository transactionRepository;
    private final BlockCardRequestRepository blockCardRequestRepository;

    public Page<CardDto> getUserCards(CardSearchRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User user = userRepository.findUserByEmail(email).orElseThrow(() -> new UserNotFoundException("User not found"));

        Page<Card> cardsPage = cardRepository.findByOwnerId(user.getId(), PageRequest.of(request.getPage(), request.getSize()));

        return cardsPage.map(CardDto::from);
    }

    public void transferBetweenCards(TransferRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User user = userRepository.findUserByEmail(email).orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!cardRepository.existsByIdAndOwnerId(request.getFromCardId(),
                user.getId()) || !cardRepository.existsByIdAndOwnerId(request.getToCardId(), user.getId())) {
            throw new AccessDeniedException("Одна из карт не принадлежит пользователю");
        }

        Card fromCard = cardRepository.findById(request.getFromCardId())
                .orElseThrow(() -> new CardNotFoundException("Карта отправителя не найдена"));
        Card toCard = cardRepository.findById(request.getToCardId())
                .orElseThrow(() -> new CardNotFoundException("Карта получателя не найдена"));

        if (!fromCard.isActive() || !toCard.isActive()) {
            throw new CardOperationException("Одна из карт заблокирована");
        }

        if (fromCard.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientException("Недостаточно средств");
        }

        fromCard.setBalance(fromCard.getBalance().subtract(request.getAmount()));
        toCard.setBalance(toCard.getBalance().add(request.getAmount()));

        cardRepository.saveAll(List.of(fromCard, toCard));

        Transactions transaction = Transactions.builder()
                .fromCard(fromCard)
                .toCard(toCard)
                .amount(request.getAmount())
                .description(request.getDescription() != null ? request.getDescription() : "Перевод между картами")
                .build();
        transactionRepository.save(transaction);
    }

    public BigDecimal getCardBalance(Long cardId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User user = userRepository.findUserByEmail(email).orElseThrow(() -> new UserNotFoundException("User not found"));

        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException("Карта не найдена"));

        if (!card.getOwner().getId().equals(user.getId())) {
            throw new AccessDeniedException("Нет доступа к карте");
        }

        return card.getBalance();
    }

    public void requestCardBlock(Long cardId, String reason) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User user = userRepository.findUserByEmail(email).orElseThrow(() -> new UserNotFoundException("User not found"));

        Card card = cardRepository.findById(cardId).orElseThrow(() -> new CardNotFoundException("Карта не найдена"));

        if (!card.getOwner().getId().equals(user.getId())) {
            throw new AccessDeniedException("Нет доступа к карте");
        }

        if (card.getStatus() == CardStatus.BLOCKED) {
            throw new CardOperationException("Карта уже заблокирована");
        }

        BlockCardRequest request = BlockCardRequest.builder()
                .card(card)
                .user(card.getOwner())
                .reason(reason)
                .status(BlockRequestStatus.PENDING)
                .build();

        blockCardRequestRepository.save(request);
    }
}

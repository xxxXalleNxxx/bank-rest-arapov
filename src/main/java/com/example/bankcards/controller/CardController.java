package com.example.bankcards.controller;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.CardSearchRequest;
import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.service.CardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;

@RestController
@RequestMapping("api/v1/cards")
@RequiredArgsConstructor
public class CardController {

    private final CardService cardService;


    @GetMapping
    public Page<CardDto> getUserCards(
            @ModelAttribute CardSearchRequest request) {
        return cardService.getUserCards(request);
    }

    @PostMapping("/{cardId}/request-block")
    public ResponseEntity<Void> blockCard(
            @PathVariable Long cardId,
            @RequestParam String reason) {
        cardService.requestCardBlock(cardId, reason);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/transfer")
    public ResponseEntity<Void> transferBetweenCards(@RequestBody @Valid TransferRequest request) {
        cardService.transferBetweenCards(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{cardId}/balance")
    public BigDecimal getCardBalance(@PathVariable Long cardId) {
        return cardService.getCardBalance(cardId);
    }
}

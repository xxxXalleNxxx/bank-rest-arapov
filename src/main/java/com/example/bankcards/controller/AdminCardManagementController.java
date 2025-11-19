package com.example.bankcards.controller;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.CreateCardRequest;
import com.example.bankcards.service.AdminCardManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/cards")
@RequiredArgsConstructor
public class AdminCardManagementController {

    private final AdminCardManagementService adminCardManagementService;

    @PostMapping()
    public ResponseEntity<CardDto> createCard(@RequestBody @Valid CreateCardRequest request) {
        CardDto cardDto = adminCardManagementService.createCard(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(cardDto);
    }

    @PostMapping("/{cardId}/block")
    public ResponseEntity<Void> blockCard(@PathVariable Long cardId) {
        adminCardManagementService.blockCard(cardId);
        return ResponseEntity.ok().build();
    }


    @PostMapping("/{cardId}/activate")
    public ResponseEntity<Void> activateCard(@PathVariable Long cardId) {
        adminCardManagementService.activateCard(cardId);
        return ResponseEntity.ok().build();
    }

    @GetMapping()
    public Page<CardDto> getAlLCards(Pageable pageable) {
        return adminCardManagementService.getAllCards(pageable);
    }

    @DeleteMapping("/{cardId}")
    public ResponseEntity<Void> deleteCard(@PathVariable Long cardId) {
        adminCardManagementService.deleteCard(cardId);
        return ResponseEntity.noContent().build();
    }
}

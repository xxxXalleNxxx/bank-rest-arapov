package com.example.bankcards.repository;

import com.example.bankcards.entity.BlockCardRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BlockCardRequestRepository extends JpaRepository<BlockCardRequest, Long> {

    @Modifying
    @Query("UPDATE BlockCardRequest b SET b.card = NULL WHERE b.card.id = :cardId")
    void setCardToNull(@Param("cardId") Long cardId);
}

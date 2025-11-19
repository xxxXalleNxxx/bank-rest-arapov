package com.example.bankcards.repository;

import com.example.bankcards.entity.Transactions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<Transactions, Long> {

    @Modifying
    @Query("UPDATE Transactions t SET t.fromCard = NULL WHERE t.fromCard.id = :cardId")
    void setFromCardToNull(@Param ("cardId") Long cardId);

    @Modifying
    @Query("UPDATE Transactions t SET t.toCard = NULL WHERE t.toCard.id = :cardId")
    void setToCardToNull(@Param ("cardId") Long cardId);
}

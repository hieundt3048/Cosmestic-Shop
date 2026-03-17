package com.cosmeticshop.cosmetic.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.cosmeticshop.cosmetic.Entity.SupportChatMessage;

@Repository
public interface SupportChatMessageRepository extends JpaRepository<SupportChatMessage, Long> {

    @Query("SELECT m FROM SupportChatMessage m " +
            "WHERE (m.sender.id = :userId AND m.recipient.id = :otherUserId) " +
            "OR (m.sender.id = :otherUserId AND m.recipient.id = :userId) " +
            "ORDER BY m.createdAt ASC, m.id ASC")
    List<SupportChatMessage> findConversation(
            @Param("userId") Long userId,
            @Param("otherUserId") Long otherUserId);

    @Query("SELECT m FROM SupportChatMessage m " +
            "WHERE m.sender.id = :userId OR m.recipient.id = :userId " +
            "ORDER BY m.createdAt DESC, m.id DESC")
    List<SupportChatMessage> findRecentForUser(@Param("userId") Long userId);
}

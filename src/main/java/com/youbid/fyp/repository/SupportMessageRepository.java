package com.youbid.fyp.repository;

import com.youbid.fyp.model.SupportChat;
import com.youbid.fyp.model.SupportMessage;
import com.youbid.fyp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface SupportMessageRepository extends JpaRepository<SupportMessage, Integer> {

    List<SupportMessage> findBySupportChatOrderByTimestampAsc(SupportChat supportChat);

    @Query("SELECT COUNT(m) FROM SupportMessage m WHERE m.supportChat = :chat AND m.sender != :user AND m.isRead = false")
    int countUnreadMessagesInSupportChat(@Param("chat") SupportChat chat, @Param("user") User user);

    @Query("SELECT m FROM SupportMessage m WHERE m.supportChat = :chat AND m.timestamp > :since ORDER BY m.timestamp ASC")
    List<SupportMessage> findMessagesSince(@Param("chat") SupportChat chat, @Param("since") LocalDateTime since);

    @Query("SELECT COUNT(m) FROM SupportMessage m WHERE m.supportChat = :chat")
    int countMessagesBySupportChat(@Param("chat") SupportChat chat);
}
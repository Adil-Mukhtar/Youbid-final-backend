// MessageRepository.java
package com.youbid.fyp.repository;

import com.youbid.fyp.model.Chat;
import com.youbid.fyp.model.Message;
import com.youbid.fyp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Integer> {
    List<Message> findByChatOrderByTimestampAsc(Chat chat);

    @Query("SELECT COUNT(m) FROM Message m WHERE m.chat = :chat AND m.sender != :user AND m.isRead = false")
    int countUnreadMessagesInChat(@Param("chat") Chat chat, @Param("user") User user);

    @Query("SELECT m FROM Message m WHERE m.chat = :chat AND m.timestamp > :since ORDER BY m.timestamp ASC")
    List<Message> findMessagesSince(@Param("chat") Chat chat, @Param("since") LocalDateTime since);
}
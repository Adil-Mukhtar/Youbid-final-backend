// ChatRepository.java
package com.youbid.fyp.repository;

import com.youbid.fyp.model.Chat;
import com.youbid.fyp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatRepository extends JpaRepository<Chat, Integer> {
    @Query("SELECT c FROM Chat c WHERE (c.user1 = :user OR c.user2 = :user) ORDER BY c.createdAt DESC")
    List<Chat> findChatsByUser(@Param("user") User user);

    @Query("SELECT c FROM Chat c WHERE (c.user1 = :user1 AND c.user2 = :user2) OR (c.user1 = :user2 AND c.user2 = :user1)")
    Optional<Chat> findChatBetweenUsers(@Param("user1") User user1, @Param("user2") User user2);

    @Query("SELECT c FROM Chat c WHERE (:query IS NULL OR LOWER(c.user1.firstname) LIKE %:query% OR LOWER(c.user1.lastname) LIKE %:query% OR LOWER(c.user2.firstname) LIKE %:query% OR LOWER(c.user2.lastname) LIKE %:query%) AND (c.user1 = :user OR c.user2 = :user)")
    List<Chat> searchChatsByUser(@Param("user") User user, @Param("query") String query);
}
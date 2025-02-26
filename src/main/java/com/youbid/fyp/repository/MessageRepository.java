package com.youbid.fyp.repository;

import com.youbid.fyp.model.Chat;
import com.youbid.fyp.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByChatOrderByTimestampAsc(Chat chat);
}

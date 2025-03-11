// ChatService.java
package com.youbid.fyp.service;

import com.youbid.fyp.model.Chat;
import com.youbid.fyp.model.Message;
import com.youbid.fyp.model.User;

import java.time.LocalDateTime;
import java.util.List;

public interface ChatService {
    List<Chat> getUserChats(Integer userId);
    Chat getChatById(Integer chatId) throws Exception;
    Chat createChat(Integer userId1, Integer userId2) throws Exception;
    List<Message> getChatMessages(Integer chatId) throws Exception;
    Message sendMessage(Integer chatId, Integer senderId, String content) throws Exception;
    List<Message> getNewMessages(Integer chatId, LocalDateTime since) throws Exception;
    void markMessagesAsRead(Integer chatId, Integer userId) throws Exception;
    List<Chat> searchChats(Integer userId, String query);
}
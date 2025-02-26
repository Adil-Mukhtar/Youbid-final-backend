package com.youbid.fyp.service;

import com.youbid.fyp.model.Chat;

public interface ChatService {
    Chat startChat(Integer loggedInUserId, Integer otherUserId) throws Exception;
    Chat findChatById(Long chatId) throws Exception;
}

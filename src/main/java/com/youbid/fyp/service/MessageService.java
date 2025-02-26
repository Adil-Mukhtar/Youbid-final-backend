package com.youbid.fyp.service;

import com.youbid.fyp.DTO.MessageDTO;
import com.youbid.fyp.model.Chat;
import com.youbid.fyp.model.User;

import java.util.List;

public interface MessageService {
    MessageDTO sendMessage(Chat chat, User sender, User receiver, String content) throws Exception;
    List<MessageDTO> getMessagesByChat(Chat chat);
}

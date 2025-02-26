package com.youbid.fyp.service;

import com.youbid.fyp.model.Chat;
import com.youbid.fyp.model.User;
import com.youbid.fyp.repository.ChatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ChatServiceImplementation implements ChatService {

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private UserService userService;

    @Override
    public Chat startChat(Integer loggedInUserId, Integer otherUserId) throws Exception {
        User loggedInUser = userService.findUserById(loggedInUserId);
        User otherUser = userService.findUserById(otherUserId);

        Optional<Chat> existingChat = chatRepository.findByUsers(loggedInUser, otherUser);
        if (existingChat.isPresent()) {
            return existingChat.get();
        }

        Chat newChat = new Chat(loggedInUser, otherUser);
        return chatRepository.save(newChat);  // 🔥 Ensure the chat is saved
    }


    @Override
    public Chat findChatById(Long chatId) throws Exception {
        return chatRepository.findById(chatId)
                .orElseThrow(() -> new Exception("Chat not found"));
    }
}

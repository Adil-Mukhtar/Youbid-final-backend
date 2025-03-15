// ChatServiceImplementation.java
package com.youbid.fyp.service;

import com.youbid.fyp.model.Chat;
import com.youbid.fyp.model.Message;
import com.youbid.fyp.model.User;
import com.youbid.fyp.repository.ChatRepository;
import com.youbid.fyp.repository.MessageRepository;
import com.youbid.fyp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ChatServiceImplementation implements ChatService {

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationService notificationService;

    @Override
    public List<Chat> getUserChats(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return chatRepository.findChatsByUser(user);
    }

    @Override
    public Chat getChatById(Integer chatId) throws Exception {
        return chatRepository.findById(chatId)
                .orElseThrow(() -> new Exception("Chat not found"));
    }

    @Override
    @Transactional
    public Chat createChat(Integer userId1, Integer userId2) throws Exception {
        if (userId1.equals(userId2)) {
            throw new Exception("Cannot create chat with yourself");
        }

        User user1 = userRepository.findById(userId1)
                .orElseThrow(() -> new Exception("User 1 not found"));

        User user2 = userRepository.findById(userId2)
                .orElseThrow(() -> new Exception("User 2 not found"));

        // Check if chat already exists
        Optional<Chat> existingChat = chatRepository.findChatBetweenUsers(user1, user2);
        if (existingChat.isPresent()) {
            return existingChat.get();
        }

        // Create new chat
        Chat newChat = new Chat(user1, user2);
        return chatRepository.save(newChat);
    }

    @Override
    public List<Message> getChatMessages(Integer chatId) throws Exception {
        Chat chat = getChatById(chatId);
        return messageRepository.findByChatOrderByTimestampAsc(chat);
    }

    @Override
    @Transactional
    public Message sendMessage(Integer chatId, Integer senderId, String content) throws Exception {
        Chat chat = getChatById(chatId);
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new Exception("Sender not found"));

        // Verify sender is part of the chat
        if (!chat.getUser1().getId().equals(senderId) && !chat.getUser2().getId().equals(senderId)) {
            throw new Exception("User is not part of this chat");
        }

        Message message = new Message(sender, content);
        message.setChat(chat);

        Message savedMessage = messageRepository.save(message);

        // Determine the recipient (the user who is not the sender)
        User recipient = chat.getUser1().getId().equals(senderId) ? chat.getUser2() : chat.getUser1();

        // Create notification for new message
        String senderName = sender.getFirstname() + " " + sender.getLastname();
        String messagePreview = content.length() > 30 ? content.substring(0, 27) + "..." : content;

        notificationService.notifyNewMessage(recipient, senderName, messagePreview, chatId);

        return savedMessage;
    }

    @Override
    public List<Message> getNewMessages(Integer chatId, LocalDateTime since) throws Exception {
        Chat chat = getChatById(chatId);
        return messageRepository.findMessagesSince(chat, since);
    }

    @Override
    @Transactional
    public void markMessagesAsRead(Integer chatId, Integer userId) throws Exception {
        Chat chat = getChatById(chatId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new Exception("User not found"));

        List<Message> messages = messageRepository.findByChatOrderByTimestampAsc(chat);
        for (Message message : messages) {
            if (!message.getSender().getId().equals(userId) && !message.isRead()) {
                message.setRead(true);
                messageRepository.save(message);
            }
        }
    }

    @Override
    public List<Chat> searchChats(Integer userId, String query) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return chatRepository.searchChatsByUser(user, query.toLowerCase());
    }
}
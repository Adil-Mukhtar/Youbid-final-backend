// ChatController.java
package com.youbid.fyp.controller;

import com.youbid.fyp.DTO.ChatDTO;
import com.youbid.fyp.DTO.MessageDTO;
import com.youbid.fyp.DTO.UserDTO;
import com.youbid.fyp.model.Chat;
import com.youbid.fyp.model.Message;
import com.youbid.fyp.model.User;
import com.youbid.fyp.repository.MessageRepository;
import com.youbid.fyp.service.ChatService;
import com.youbid.fyp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/chats")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @Autowired
    private UserService userService;

    @Autowired
    private MessageRepository messageRepository;

    @GetMapping
    public ResponseEntity<List<ChatDTO>> getUserChats(@RequestHeader("Authorization") String jwt) {
        try {
            User currentUser = userService.findUserByJwt(jwt);
            List<Chat> chats = chatService.getUserChats(currentUser.getId());
            List<ChatDTO> chatDTOs = new ArrayList<>();

            for (Chat chat : chats) {
                User otherUser = chat.getUser1().getId().equals(currentUser.getId()) ?
                        chat.getUser2() : chat.getUser1();

                String lastMessage = "";
                LocalDateTime lastMessageTime = chat.getCreatedAt();

                List<Message> messages = chat.getMessages();
                if (!messages.isEmpty()) {
                    Message lastMsg = messages.get(messages.size() - 1);
                    lastMessage = lastMsg.getContent();
                    if (lastMessage.length() > 30) {
                        lastMessage = lastMessage.substring(0, 27) + "...";
                    }
                    lastMessageTime = lastMsg.getTimestamp();
                }

                int unreadCount = messageRepository.countUnreadMessagesInChat(chat, currentUser);

                ChatDTO dto = new ChatDTO(
                        chat.getId(),
                        new UserDTO(otherUser.getId(), otherUser.getFirstname(), otherUser.getLastname()),
                        lastMessage,
                        lastMessageTime,
                        unreadCount
                );

                chatDTOs.add(dto);
            }

            return new ResponseEntity<>(chatDTOs, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{chatId}")
    public ResponseEntity<?> getChatMessages(
            @PathVariable Integer chatId,
            @RequestHeader("Authorization") String jwt) {
        try {
            User currentUser = userService.findUserByJwt(jwt);
            Chat chat = chatService.getChatById(chatId);

            // Verify user is part of the chat
            if (!chat.getUser1().getId().equals(currentUser.getId()) &&
                    !chat.getUser2().getId().equals(currentUser.getId())) {
                return new ResponseEntity<>("User not authorized to view this chat", HttpStatus.FORBIDDEN);
            }

            List<Message> messages = chatService.getChatMessages(chatId);

            // Mark messages as read
            chatService.markMessagesAsRead(chatId, currentUser.getId());

            List<MessageDTO> messageDTOs = messages.stream()
                    .map(message -> new MessageDTO(
                            message.getId(),
                            message.getSender().getId(),
                            message.getSender().getFirstname() + " " + message.getSender().getLastname(),
                            message.getContent(),
                            message.getTimestamp(),
                            message.isRead(),
                            message.getSender().getId().equals(currentUser.getId())
                    ))
                    .collect(Collectors.toList());

            return new ResponseEntity<>(messageDTOs, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/{chatId}/send")
    public ResponseEntity<?> sendMessage(
            @PathVariable Integer chatId,
            @RequestBody Map<String, String> payload,
            @RequestHeader("Authorization") String jwt) {
        try {
            if (!payload.containsKey("content") || payload.get("content").trim().isEmpty()) {
                return new ResponseEntity<>("Message content cannot be empty", HttpStatus.BAD_REQUEST);
            }

            User sender = userService.findUserByJwt(jwt);
            Message message = chatService.sendMessage(chatId, sender.getId(), payload.get("content"));

            MessageDTO messageDTO = new MessageDTO(
                    message.getId(),
                    message.getSender().getId(),
                    message.getSender().getFirstname() + " " + message.getSender().getLastname(),
                    message.getContent(),
                    message.getTimestamp(),
                    message.isRead(),
                    true
            );

            return new ResponseEntity<>(messageDTO, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{chatId}/poll")
    public ResponseEntity<?> pollNewMessages(
            @PathVariable Integer chatId,
            @RequestParam("since") String sinceStr,
            @RequestHeader("Authorization") String jwt) {
        try {
            User currentUser = userService.findUserByJwt(jwt);
            Chat chat = chatService.getChatById(chatId);

            // Verify user is part of the chat
            if (!chat.getUser1().getId().equals(currentUser.getId()) &&
                    !chat.getUser2().getId().equals(currentUser.getId())) {
                return new ResponseEntity<>("User not authorized to view this chat", HttpStatus.FORBIDDEN);
            }

            LocalDateTime since = LocalDateTime.parse(sinceStr);
            List<Message> newMessages = chatService.getNewMessages(chatId, since);

            // Mark messages as read
            chatService.markMessagesAsRead(chatId, currentUser.getId());

            List<MessageDTO> messageDTOs = newMessages.stream()
                    .map(message -> new MessageDTO(
                            message.getId(),
                            message.getSender().getId(),
                            message.getSender().getFirstname() + " " + message.getSender().getLastname(),
                            message.getContent(),
                            message.getTimestamp(),
                            message.isRead(),
                            message.getSender().getId().equals(currentUser.getId())
                    ))
                    .collect(Collectors.toList());

            return new ResponseEntity<>(messageDTOs, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/start/{userId}")
    public ResponseEntity<?> startChat(
            @PathVariable Integer userId,
            @RequestHeader("Authorization") String jwt) {
        try {
            User currentUser = userService.findUserByJwt(jwt);
            Chat chat = chatService.createChat(currentUser.getId(), userId);

            User otherUser = chat.getUser1().getId().equals(currentUser.getId()) ?
                    chat.getUser2() : chat.getUser1();

            Map<String, Object> response = new HashMap<>();
            response.put("id", chat.getId());
            response.put("otherUser", new UserDTO(
                    otherUser.getId(),
                    otherUser.getFirstname(),
                    otherUser.getLastname()
            ));

            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<ChatDTO>> searchChats(
            @RequestParam("query") String query,
            @RequestHeader("Authorization") String jwt) {
        try {
            User currentUser = userService.findUserByJwt(jwt);
            List<Chat> chats = chatService.searchChats(currentUser.getId(), query);

            List<ChatDTO> chatDTOs = new ArrayList<>();
            for (Chat chat : chats) {
                User otherUser = chat.getUser1().getId().equals(currentUser.getId()) ?
                        chat.getUser2() : chat.getUser1();

                String lastMessage = "";
                LocalDateTime lastMessageTime = chat.getCreatedAt();

                List<Message> messages = chat.getMessages();
                if (!messages.isEmpty()) {
                    Message lastMsg = messages.get(messages.size() - 1);
                    lastMessage = lastMsg.getContent();
                    if (lastMessage.length() > 30) {
                        lastMessage = lastMessage.substring(0, 27) + "...";
                    }
                    lastMessageTime = lastMsg.getTimestamp();
                }

                int unreadCount = messageRepository.countUnreadMessagesInChat(chat, currentUser);

                ChatDTO dto = new ChatDTO(
                        chat.getId(),
                        new UserDTO(otherUser.getId(), otherUser.getFirstname(), otherUser.getLastname()),
                        lastMessage,
                        lastMessageTime,
                        unreadCount
                );

                chatDTOs.add(dto);
            }

            return new ResponseEntity<>(chatDTOs, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
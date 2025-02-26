package com.youbid.fyp.controller;

import com.youbid.fyp.DTO.MessageDTO;
import com.youbid.fyp.model.Chat;
import com.youbid.fyp.model.Message;
import com.youbid.fyp.model.User;
import com.youbid.fyp.service.ChatService;
import com.youbid.fyp.service.MessageService;
import com.youbid.fyp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @Autowired
    private ChatService chatService;

    @Autowired
    private UserService userService;

    @PostMapping("/send/{chatId}")
    public MessageDTO sendMessage(@PathVariable Long chatId,
                                  @RequestHeader("Authorization") String jwt,
                                  @RequestBody Map<String, Object> request) throws Exception {
        Chat chat = chatService.findChatById(chatId);
        User sender = userService.findUserByJwt(jwt);

        String content = (String) request.get("content");
        if (content == null || content.isEmpty()) {
            throw new IllegalArgumentException("Message content cannot be empty.");
        }

        User receiver = chat.getUser1().getId().equals(sender.getId()) ? chat.getUser2() : chat.getUser1();

        return messageService.sendMessage(chat, sender, receiver, content);
    }

    @GetMapping("/chat/{chatId}")
    public List<MessageDTO> getChatMessages(@PathVariable Long chatId) throws Exception {
        Chat chat = chatService.findChatById(chatId);
        return messageService.getMessagesByChat(chat);
    }

}

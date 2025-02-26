package com.youbid.fyp.controller;

import com.youbid.fyp.model.Chat;
import com.youbid.fyp.model.User;
import com.youbid.fyp.service.ChatService;
import com.youbid.fyp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chats")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @Autowired
    private UserService userService;

    @PostMapping("/start/{otherUserId}")
    public Chat startChat(@PathVariable Integer otherUserId, @RequestHeader("Authorization") String jwt) throws Exception {
        User loggedInUser = userService.findUserByJwt(jwt);
        return chatService.startChat(loggedInUser.getId(), otherUserId);
    }
}

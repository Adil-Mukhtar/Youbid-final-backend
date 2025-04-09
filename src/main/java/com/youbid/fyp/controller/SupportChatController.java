package com.youbid.fyp.controller;

import com.youbid.fyp.DTO.SupportChatDTO;
import com.youbid.fyp.DTO.SupportMessageDTO;
import com.youbid.fyp.DTO.UserDTO;
import com.youbid.fyp.model.SupportChat;
import com.youbid.fyp.model.SupportMessage;
import com.youbid.fyp.model.User;
import com.youbid.fyp.repository.SupportMessageRepository;
import com.youbid.fyp.service.ActivityService;
import com.youbid.fyp.service.SupportChatService;
import com.youbid.fyp.service.SupportStaffService;
import com.youbid.fyp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/support-chats")
public class SupportChatController {

    @Autowired
    private SupportChatService supportChatService;

    @Autowired
    private UserService userService;

    @Autowired
    private SupportStaffService supportStaffService;

    @Autowired
    private SupportMessageRepository supportMessageRepository;

    @Autowired
    private ActivityService activityService; // Add this at the class level

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'SUPPORT')")
    public ResponseEntity<List<SupportChatDTO>> getUserSupportChats(@RequestHeader("Authorization") String jwt) {
        try {
            User currentUser = userService.findUserByJwt(jwt);
            List<SupportChat> chats = supportChatService.getUserSupportChats(currentUser.getId());
            List<SupportChatDTO> chatDTOs = convertToChatDTOs(chats, currentUser);

            return new ResponseEntity<>(chatDTOs, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/support-agent")
    @PreAuthorize("hasRole('SUPPORT')")
    public ResponseEntity<List<SupportChatDTO>> getSupportAgentChats(@RequestHeader("Authorization") String jwt) {
        try {
            User currentUser = userService.findUserByJwt(jwt);
            List<SupportChat> chats = supportChatService.getSupportAgentChats(currentUser.getId());
            List<SupportChatDTO> chatDTOs = convertToChatDTOs(chats, currentUser);

            return new ResponseEntity<>(chatDTOs, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/unassigned")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPPORT')")
    public ResponseEntity<List<SupportChatDTO>> getUnassignedChats() {
        try {
            List<SupportChat> chats = supportChatService.getUnassignedSupportChats();
            List<SupportChatDTO> chatDTOs = chats.stream()
                    .map(this::convertToChatDTOSimple)
                    .collect(Collectors.toList());

            return new ResponseEntity<>(chatDTOs, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/unassigned/{department}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPPORT')")
    public ResponseEntity<List<SupportChatDTO>> getUnassignedChatsByDepartment(@PathVariable String department) {
        try {
            List<SupportChat> chats = supportChatService.getUnassignedSupportChatsByDepartment(department);
            List<SupportChatDTO> chatDTOs = chats.stream()
                    .map(this::convertToChatDTOSimple)
                    .collect(Collectors.toList());

            return new ResponseEntity<>(chatDTOs, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPPORT')")
    public ResponseEntity<List<SupportChatDTO>> getChatsByStatus(@PathVariable String status) {
        try {
            List<SupportChat> chats = supportChatService.getChatsByStatus(status);
            List<SupportChatDTO> chatDTOs = chats.stream()
                    .map(this::convertToChatDTOSimple)
                    .collect(Collectors.toList());

            return new ResponseEntity<>(chatDTOs, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPPORT')")
    public ResponseEntity<List<SupportChatDTO>> searchSupportChats(@RequestParam String query) {
        try {
            List<SupportChat> chats = supportChatService.searchSupportChats(query);
            List<SupportChatDTO> chatDTOs = chats.stream()
                    .map(this::convertToChatDTOSimple)
                    .collect(Collectors.toList());

            return new ResponseEntity<>(chatDTOs, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{chatId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'SUPPORT')")
    public ResponseEntity<?> getSupportChatMessages(
            @PathVariable Integer chatId,
            @RequestHeader("Authorization") String jwt) {
        try {
            User currentUser = userService.findUserByJwt(jwt);
            SupportChat chat = supportChatService.getSupportChatById(chatId);

            // Verify user is part of the chat or is an admin
            if (!isAuthorized(chat, currentUser)) {
                return new ResponseEntity<>("User not authorized to view this chat", HttpStatus.FORBIDDEN);
            }

            List<SupportMessage> messages = supportChatService.getChatMessages(chatId);

            // Mark messages as read
            supportChatService.markMessagesAsRead(chatId, currentUser.getId());

            List<SupportMessageDTO> messageDTOs = messages.stream()
                    .map(message -> convertToMessageDTO(message, currentUser))
                    .collect(Collectors.toList());

            return new ResponseEntity<>(messageDTOs, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> createSupportChat(
            @RequestBody Map<String, String> request,
            @RequestHeader("Authorization") String jwt) {
        try {
            User currentUser = userService.findUserByJwt(jwt);

            String department = request.get("department");
            String topic = request.get("topic");

            if (department == null || topic == null) {
                return new ResponseEntity<>("Department and topic are required", HttpStatus.BAD_REQUEST);
            }

            SupportChat supportChat = supportChatService.createSupportChat(currentUser.getId(), department, topic);
            SupportChatDTO chatDTO = convertToChatDTOSimple(supportChat);

            // Track activity
            activityService.trackSupportActivity(
                    "New Support Ticket",
                    String.format("%s %s created a support ticket about: %s",
                            currentUser.getFirstname(), currentUser.getLastname(), topic),
                    supportChat.getId()
            );

            return new ResponseEntity<>(chatDTO, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/{chatId}/send")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'SUPPORT')")
    public ResponseEntity<?> sendMessage(
            @PathVariable Integer chatId,
            @RequestBody Map<String, String> payload,
            @RequestHeader("Authorization") String jwt) {
        try {
            if (!payload.containsKey("content") || payload.get("content").trim().isEmpty()) {
                return new ResponseEntity<>("Message content cannot be empty", HttpStatus.BAD_REQUEST);
            }

            User sender = userService.findUserByJwt(jwt);
            SupportChat chat = supportChatService.getSupportChatById(chatId);

            // Verify user is part of the chat or is an admin
            if (!isAuthorized(chat, sender)) {
                return new ResponseEntity<>("User not authorized to send messages in this chat", HttpStatus.FORBIDDEN);
            }

            // Determine if message is from support
            boolean isFromSupport = "SUPPORT".equals(sender.getRole()) || "ADMIN".equals(sender.getRole());

            SupportMessage message = supportChatService.sendMessage(
                    chatId, sender.getId(), payload.get("content"), isFromSupport);

            SupportMessageDTO messageDTO = convertToMessageDTO(message, sender);

            return new ResponseEntity<>(messageDTO, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{chatId}/assign")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPPORT')")
    public ResponseEntity<?> assignSupportChat(
            @PathVariable Integer chatId,
            @RequestBody(required = false) Map<String, Integer> request,
            @RequestHeader("Authorization") String jwt) {
        try {
            User currentUser = userService.findUserByJwt(jwt);
            Integer supportStaffId = null;

            if (request != null && request.containsKey("supportStaffId")) {
                supportStaffId = request.get("supportStaffId");
            } else {
                // If no support staff ID is provided, assign to the current user if they are support staff
                if ("SUPPORT".equals(currentUser.getRole())) {
                    try {
                        supportStaffId = supportStaffService.findSupportStaffByUserId(currentUser.getId()).getId();
                    } catch (Exception e) {
                        return new ResponseEntity<>("Support staff ID is required", HttpStatus.BAD_REQUEST);
                    }
                } else {
                    return new ResponseEntity<>("Support staff ID is required", HttpStatus.BAD_REQUEST);
                }
            }

            SupportChat supportChat = supportChatService.assignSupportChat(chatId, supportStaffId);
            SupportChatDTO chatDTO = convertToChatDTOSimple(supportChat);

            // Get support staff user
            User supportAgent = supportChat.getSupportAgent();

            // Track activity
            activityService.trackSupportActivity(
                    "Support Ticket Assigned",
                    String.format("Support ticket #%d assigned to %s %s",
                            chatId, supportAgent.getFirstname(), supportAgent.getLastname()),
                    chatId
            );

            return new ResponseEntity<>(chatDTO, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{chatId}/close")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPPORT')")
    public ResponseEntity<?> closeSupportChat(
            @PathVariable Integer chatId,
            @RequestHeader("Authorization") String jwt) {
        try {
            User currentUser = userService.findUserByJwt(jwt);
            SupportChat chat = supportChatService.getSupportChatById(chatId);

            // Verify user is the assigned support agent or an admin
            if (!"ADMIN".equals(currentUser.getRole()) &&
                    (chat.getSupportAgent() == null || !chat.getSupportAgent().getId().equals(currentUser.getId()))) {
                return new ResponseEntity<>("Only the assigned support agent or an admin can close this chat", HttpStatus.FORBIDDEN);
            }

            SupportChat supportChat = supportChatService.closeSupportChat(chatId);
            SupportChatDTO chatDTO = convertToChatDTOSimple(supportChat);

            // Track activity
            activityService.trackSupportActivity(
                    "Support Ticket Resolved",
                    String.format("Support ticket #%d about '%s' was resolved", chatId, chat.getTopic()),
                    chatId
            );

            return new ResponseEntity<>(chatDTO, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{chatId}/poll")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'SUPPORT')")
    public ResponseEntity<?> pollNewMessages(
            @PathVariable Integer chatId,
            @RequestParam("since") String sinceStr,
            @RequestHeader("Authorization") String jwt) {
        try {
            User currentUser = userService.findUserByJwt(jwt);
            SupportChat chat = supportChatService.getSupportChatById(chatId);

            // Verify user is part of the chat or is an admin
            if (!isAuthorized(chat, currentUser)) {
                return new ResponseEntity<>("User not authorized to view this chat", HttpStatus.FORBIDDEN);
            }

            LocalDateTime since = LocalDateTime.parse(sinceStr);
            List<SupportMessage> newMessages = supportChatService.getNewMessages(chatId, since);

            // Mark messages as read
            supportChatService.markMessagesAsRead(chatId, currentUser.getId());

            List<SupportMessageDTO> messageDTOs = newMessages.stream()
                    .map(message -> convertToMessageDTO(message, currentUser))
                    .collect(Collectors.toList());

            return new ResponseEntity<>(messageDTOs, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Helper methods to convert models to DTOs
    private List<SupportChatDTO> convertToChatDTOs(List<SupportChat> chats, User currentUser) throws Exception {
        List<SupportChatDTO> chatDTOs = new ArrayList<>();

        for (SupportChat chat : chats) {
            UserDTO userDTO = new UserDTO(
                    chat.getUser().getId(),
                    chat.getUser().getFirstname(),
                    chat.getUser().getLastname()
            );

            UserDTO supportAgentDTO = chat.getSupportAgent() != null
                    ? new UserDTO(
                    chat.getSupportAgent().getId(),
                    chat.getSupportAgent().getFirstname(),
                    chat.getSupportAgent().getLastname()
            )
                    : null;

            String lastMessage = "";
            LocalDateTime lastActivityTime = chat.getCreatedAt();

            // This assumes chat.getMessages() is available - adjust if needed
            List<SupportMessage> messages = supportChatService.getChatMessages(chat.getId());
            if (!messages.isEmpty()) {
                SupportMessage lastMsg = messages.get(messages.size() - 1);
                lastMessage = lastMsg.getContent();
                if (lastMessage.length() > 30) {
                    lastMessage = lastMessage.substring(0, 27) + "...";
                }
                lastActivityTime = lastMsg.getTimestamp();
            }

            int unreadCount = supportMessageRepository.countUnreadMessagesInSupportChat(chat, currentUser);

            SupportChatDTO dto = new SupportChatDTO(
                    chat.getId(),
                    userDTO,
                    supportAgentDTO,
                    chat.getDepartment(),
                    chat.getStatus(),
                    chat.getTopic(),
                    chat.getCreatedAt(),
                    lastActivityTime,
                    lastMessage,
                    unreadCount
            );

            chatDTOs.add(dto);
        }

        return chatDTOs;
    }

    private SupportChatDTO convertToChatDTOSimple(SupportChat chat) {
        UserDTO userDTO = new UserDTO(
                chat.getUser().getId(),
                chat.getUser().getFirstname(),
                chat.getUser().getLastname()
        );

        UserDTO supportAgentDTO = chat.getSupportAgent() != null
                ? new UserDTO(
                chat.getSupportAgent().getId(),
                chat.getSupportAgent().getFirstname(),
                chat.getSupportAgent().getLastname()
        )
                : null;

        return new SupportChatDTO(
                chat.getId(),
                userDTO,
                supportAgentDTO,
                chat.getDepartment(),
                chat.getStatus(),
                chat.getTopic(),
                chat.getCreatedAt(),
                chat.getLastActivityTime(),
                null,
                0
        );
    }

    private SupportMessageDTO convertToMessageDTO(SupportMessage message, User currentUser) {
        return new SupportMessageDTO(
                message.getId(),
                message.getSender().getId(),
                message.getSender().getFirstname() + " " + message.getSender().getLastname(),
                message.getContent(),
                message.getTimestamp(),
                message.isRead(),
                message.isFromSupport(),
                message.getSender().getId().equals(currentUser.getId())
        );
    }

    // Helper method to check if a user is authorized to access a chat
    private boolean isAuthorized(SupportChat chat, User user) {
        // User is authorized if they are either the chat creator, the assigned support agent, or an admin
        return user.getId().equals(chat.getUser().getId()) ||
                (chat.getSupportAgent() != null && user.getId().equals(chat.getSupportAgent().getId())) ||
                "ADMIN".equals(user.getRole());
    }
}
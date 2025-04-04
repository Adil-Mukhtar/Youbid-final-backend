package com.youbid.fyp.service;

import com.youbid.fyp.model.*;
import com.youbid.fyp.repository.SupportChatRepository;
import com.youbid.fyp.repository.SupportMessageRepository;
import com.youbid.fyp.repository.SupportStaffRepository;
import com.youbid.fyp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class SupportChatServiceImplementation implements SupportChatService {

    @Autowired
    private SupportChatRepository supportChatRepository;

    @Autowired
    private SupportMessageRepository supportMessageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SupportStaffRepository supportStaffRepository;

    @Autowired
    private SupportStaffService supportStaffService;

    @Autowired
    private NotificationService notificationService;

    @Override
    @Transactional
    public SupportChat createSupportChat(Integer userId, String department, String topic) throws Exception {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new Exception("User not found with ID: " + userId));

        SupportChat supportChat = new SupportChat(user, department, topic);

        // Save the chat first to get an ID
        SupportChat savedChat = supportChatRepository.save(supportChat);

        // Create initial system message
        SupportMessage systemMessage = new SupportMessage();
        systemMessage.setContent("Support chat created. Please wait for a support agent to assist you.");
        systemMessage.setSender(user); // Set sender as the user for now (system message)
        systemMessage.setFromSupport(true); // Mark as from system/support
        systemMessage.setRead(false);
        systemMessage.setSupportChat(savedChat);

        supportMessageRepository.save(systemMessage);

        // Try to auto-assign to an available support agent
        List<SupportStaff> availableSupportStaff = supportStaffRepository.findAvailableSupportStaffByDepartment(department);
        if (!availableSupportStaff.isEmpty()) {
            SupportStaff supportStaff = availableSupportStaff.get(0);
            savedChat.setSupportAgent(supportStaff.getUser());
            supportStaffService.assignChatToSupportStaff(supportStaff.getId());

            // Create a notification for the support agent
            notificationService.createNotification(
                    "support_chat",
                    "New Support Chat",
                    "You have been assigned a new support chat with topic: " + topic,
                    supportStaff.getUser(),
                    null,
                    null
            );
        }

        return supportChatRepository.save(savedChat);
    }

    @Override
    @Transactional
    public SupportChat assignSupportChat(Integer chatId, Integer supportStaffId) throws Exception {
        SupportChat supportChat = supportChatRepository.findById(chatId)
                .orElseThrow(() -> new Exception("Support chat not found with ID: " + chatId));

        SupportStaff supportStaff = supportStaffRepository.findById(supportStaffId)
                .orElseThrow(() -> new Exception("Support staff not found with ID: " + supportStaffId));

        // Update the support staff's active chats count
        supportStaffService.assignChatToSupportStaff(supportStaffId);

        // Assign the support agent to the chat
        supportChat.setSupportAgent(supportStaff.getUser());

        // Create system message about assignment
        SupportMessage systemMessage = new SupportMessage();
        systemMessage.setContent(supportStaff.getUser().getFirstname() + " " +
                supportStaff.getUser().getLastname() + " has joined the chat.");
        systemMessage.setSender(supportStaff.getUser());
        systemMessage.setFromSupport(true);
        systemMessage.setRead(false);
        systemMessage.setSupportChat(supportChat);

        supportMessageRepository.save(systemMessage);

        // Create a notification for the customer
        notificationService.createNotification(
                "support_chat",
                "Support Agent Assigned",
                "A support agent has been assigned to your chat",
                supportChat.getUser(),
                null,
                null
        );

        return supportChatRepository.save(supportChat);
    }

    @Override
    @Transactional
    public SupportChat closeSupportChat(Integer chatId) throws Exception {
        SupportChat supportChat = supportChatRepository.findById(chatId)
                .orElseThrow(() -> new Exception("Support chat not found with ID: " + chatId));

        if (supportChat.getSupportAgent() != null) {
            // Find support staff for this agent
            Optional<SupportStaff> supportStaff = supportStaffRepository.findByUser(supportChat.getSupportAgent());

            if (supportStaff.isPresent()) {
                // Update the support staff's active chats count
                supportStaffService.finishChatForSupportStaff(supportStaff.get().getId());
            }
        }

        // Close the chat
        supportChat.closeChat();

        // Create system message about closure
        SupportMessage systemMessage = new SupportMessage();
        systemMessage.setContent("This support chat has been closed.");
        systemMessage.setSender(supportChat.getSupportAgent() != null ?
                supportChat.getSupportAgent() : supportChat.getUser());
        systemMessage.setFromSupport(true);
        systemMessage.setRead(false);
        systemMessage.setSupportChat(supportChat);

        supportMessageRepository.save(systemMessage);

        return supportChatRepository.save(supportChat);
    }

    @Override
    public SupportChat getSupportChatById(Integer chatId) throws Exception {
        return supportChatRepository.findById(chatId)
                .orElseThrow(() -> new Exception("Support chat not found with ID: " + chatId));
    }

    @Override
    public List<SupportChat> getUserSupportChats(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        return supportChatRepository.findUserSupportChats(user);
    }

    @Override
    public List<SupportChat> getSupportAgentChats(Integer supportAgentId) {
        User supportAgent = userRepository.findById(supportAgentId)
                .orElseThrow(() -> new RuntimeException("Support agent not found with ID: " + supportAgentId));

        return supportChatRepository.findBySupportAgentOrderByLastActivityTimeDesc(supportAgent);
    }

    @Override
    public List<SupportChat> getUnassignedSupportChats() {
        return supportChatRepository.findUnassignedChats();
    }

    @Override
    public List<SupportChat> getUnassignedSupportChatsByDepartment(String department) {
        return supportChatRepository.findUnassignedChatsByDepartment(department);
    }

    @Override
    public List<SupportChat> getChatsByStatus(String status) {
        return supportChatRepository.findChatsByStatus(status);
    }

    @Override
    public List<SupportChat> searchSupportChats(String query) {
        return supportChatRepository.searchSupportChats(query.toLowerCase());
    }

    @Override
    public List<SupportMessage> getChatMessages(Integer chatId) throws Exception {
        SupportChat supportChat = getSupportChatById(chatId);
        return supportMessageRepository.findBySupportChatOrderByTimestampAsc(supportChat);
    }

    @Override
    @Transactional
    public SupportMessage sendMessage(Integer chatId, Integer senderId, String content, boolean isFromSupport) throws Exception {
        SupportChat supportChat = getSupportChatById(chatId);
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new Exception("User not found with ID: " + senderId));

        // Check if the sender is authorized to send messages in this chat
        if (!isAuthorizedSender(supportChat, sender, isFromSupport)) {
            throw new Exception("You are not authorized to send messages in this chat");
        }

        // Check if the chat is open
        if (!"open".equals(supportChat.getStatus())) {
            throw new Exception("Cannot send messages to a closed chat");
        }

        SupportMessage message = new SupportMessage(sender, content, isFromSupport);
        message.setSupportChat(supportChat);

        // Update chat's last activity time
        supportChat.setLastActivityTime(LocalDateTime.now());
        supportChatRepository.save(supportChat);

        SupportMessage savedMessage = supportMessageRepository.save(message);

        // Send notification to the recipient
        User recipient = sender.getId().equals(supportChat.getUser().getId())
                ? supportChat.getSupportAgent()
                : supportChat.getUser();

        if (recipient != null) {
            String senderName = sender.getFirstname() + " " + sender.getLastname();
            String messagePreview = content.length() > 30 ? content.substring(0, 27) + "..." : content;

            notificationService.createNotification(
                    "support_message",
                    "New Support Message",
                    senderName + ": " + messagePreview,
                    recipient,
                    null,
                    null
            );
        }

        return savedMessage;
    }

    @Override
    @Transactional
    public void markMessagesAsRead(Integer chatId, Integer userId) throws Exception {
        SupportChat supportChat = getSupportChatById(chatId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new Exception("User not found with ID: " + userId));

        // Check if the user is authorized to read messages in this chat
        if (!isAuthorizedReader(supportChat, user)) {
            throw new Exception("You are not authorized to access this chat");
        }

        List<SupportMessage> messages = supportMessageRepository.findBySupportChatOrderByTimestampAsc(supportChat);
        for (SupportMessage message : messages) {
            // Mark as read if the message is not from this user
            if (!message.getSender().getId().equals(userId) && !message.isRead()) {
                message.setRead(true);
                supportMessageRepository.save(message);
            }
        }
    }

    @Override
    public List<SupportMessage> getNewMessages(Integer chatId, LocalDateTime since) throws Exception {
        SupportChat supportChat = getSupportChatById(chatId);
        return supportMessageRepository.findMessagesSince(supportChat, since);
    }

    // Helper method to check if a user is authorized to send messages
    private boolean isAuthorizedSender(SupportChat supportChat, User sender, boolean isFromSupport) {
        if (isFromSupport) {
            // For support messages, check if sender is the assigned support agent
            return supportChat.getSupportAgent() != null &&
                    sender.getId().equals(supportChat.getSupportAgent().getId());
        } else {
            // For user messages, check if sender is the chat creator
            return sender.getId().equals(supportChat.getUser().getId());
        }
    }

    // Helper method to check if a user is authorized to read messages
    private boolean isAuthorizedReader(SupportChat supportChat, User user) {
        // User is authorized if they are either the chat creator or the assigned support agent
        return user.getId().equals(supportChat.getUser().getId()) ||
                (supportChat.getSupportAgent() != null &&
                        user.getId().equals(supportChat.getSupportAgent().getId())) ||
                user.getRole().equals("ADMIN"); // Admins can access all chats
    }
}
package com.youbid.fyp.service;

import com.youbid.fyp.model.SupportChat;
import com.youbid.fyp.model.SupportMessage;
import com.youbid.fyp.model.User;

import java.time.LocalDateTime;
import java.util.List;

public interface SupportChatService {
    SupportChat createSupportChat(Integer userId, String department, String topic) throws Exception;
    SupportChat assignSupportChat(Integer chatId, Integer supportStaffId) throws Exception;
    SupportChat closeSupportChat(Integer chatId) throws Exception;
    SupportChat getSupportChatById(Integer chatId) throws Exception;
    List<SupportChat> getUserSupportChats(Integer userId);
    List<SupportChat> getSupportAgentChats(Integer supportAgentId);
    List<SupportChat> getUnassignedSupportChats();
    List<SupportChat> getUnassignedSupportChatsByDepartment(String department);
    List<SupportChat> getChatsByStatus(String status);
    List<SupportChat> searchSupportChats(String query);
    List<SupportMessage> getChatMessages(Integer chatId) throws Exception;
    SupportMessage sendMessage(Integer chatId, Integer senderId, String content, boolean isFromSupport) throws Exception;
    void markMessagesAsRead(Integer chatId, Integer userId) throws Exception;
    List<SupportMessage> getNewMessages(Integer chatId, LocalDateTime since) throws Exception;
}
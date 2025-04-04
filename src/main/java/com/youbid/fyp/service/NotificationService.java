package com.youbid.fyp.service;

import com.youbid.fyp.model.Notification;
import com.youbid.fyp.model.User;
import java.util.List;

public interface NotificationService {
    Notification createNotification(String type, String title, String message, User user, Integer productId, Integer chatId);
    List<Notification> getUserNotifications(Integer userId);
    List<Notification> getUnreadNotifications(Integer userId);
    int getUnreadCount(Integer userId);
    void markAsRead(Integer notificationId) throws Exception;
    void markAllAsRead(Integer userId) throws Exception;

    // Helper methods to create specific notification types
    void notifyOutbid(User user, String productName, Double newBidAmount, Integer productId);
    void notifyHighestBidder(User user, String productName, Double bidAmount, Integer productId);
    void notifyAuctionEnding(User user, String productName, Integer hoursLeft, Integer productId);
    void notifyAuctionWon(User user, String productName, Double winningBid, Integer productId);
    void notifyNewMessage(User user, String senderName, String messagePreview, Integer chatId);

    // Add to NotificationService.java interface
    void notifySupportChatCreated(User user, String topic, Integer chatId);
    void notifySupportChatAssigned(User supportAgent, String topic, Integer chatId);
    void notifySupportMessage(User recipient, String senderName, String messagePreview, Integer chatId);
}
package com.youbid.fyp.service;

import com.youbid.fyp.model.Notification;
import com.youbid.fyp.model.User;
import com.youbid.fyp.repository.NotificationRepository;
import com.youbid.fyp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationServiceImplementation implements NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public Notification createNotification(String type, String title, String message, User user, Integer productId, Integer chatId) {
        Notification notification = new Notification();
        notification.setType(type);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setUser(user);
        notification.setProductId(productId);
        notification.setChatId(chatId);
        return notificationRepository.save(notification);
    }

    @Override
    public List<Notification> getUserNotifications(Integer userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        return notificationRepository.findByUserOrderByCreatedAtDesc(user);
    }

    @Override
    public List<Notification> getUnreadNotifications(Integer userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        return notificationRepository.findByUserAndIsReadFalseOrderByCreatedAtDesc(user);
    }

    @Override
    public int getUnreadCount(Integer userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        return notificationRepository.countUnreadNotifications(user);
    }

    @Override
    public void markAsRead(Integer notificationId) throws Exception {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new Exception("Notification not found"));
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Override
    public void markAllAsRead(Integer userId) throws Exception {
        User user = userRepository.findById(userId).orElseThrow(() -> new Exception("User not found"));
        List<Notification> notifications = notificationRepository.findByUserAndIsReadFalseOrderByCreatedAtDesc(user);
        for (Notification notification : notifications) {
            notification.setRead(true);
            notificationRepository.save(notification);
        }
    }

    @Override
    public void notifyOutbid(User user, String productName, Double newBidAmount, Integer productId) {
        String title = "You have been outbid!";
        String message = "Someone placed a higher bid of $" + newBidAmount + " on \"" + productName + "\"";
        createNotification("outbid", title, message, user, productId, null);
    }

    @Override
    public void notifyHighestBidder(User user, String productName, Double bidAmount, Integer productId) {
        String title = "You are the highest bidder!";
        String message = "Your bid of $" + bidAmount + " on \"" + productName + "\" is currently winning";
        createNotification("highest", title, message, user, productId, null);
    }

    @Override
    public void notifyAuctionEnding(User user, String productName, Integer hoursLeft, Integer productId) {
        String title = "Auction ending soon";
        String message = "The auction for \"" + productName + "\" ends in " + hoursLeft + " hours";
        createNotification("ending", title, message, user, productId, null);
    }

    @Override
    public void notifyAuctionWon(User user, String productName, Double winningBid, Integer productId) {
        String title = "You won the auction!";
        String message = "Congratulations! You won \"" + productName + "\" with a bid of $" + winningBid;
        createNotification("won", title, message, user, productId, null);
    }

    @Override
    public void notifyNewMessage(User user, String senderName, String messagePreview, Integer chatId) {
        String title = "New message received";
        String message = senderName + ": " + messagePreview;
        createNotification("message", title, message, user, null, chatId);
    }

    // Add to NotificationServiceImplementation.java
    @Override
    public void notifySupportChatCreated(User user, String topic, Integer chatId) {
        String title = "New Support Chat";
        String message = "Your support request about \"" + topic + "\" has been created";
        createNotification("support_chat", title, message, user, null, chatId);
    }

    @Override
    public void notifySupportChatAssigned(User supportAgent, String topic, Integer chatId) {
        String title = "Support Chat Assigned";
        String message = "You have been assigned to a support chat about \"" + topic + "\"";
        createNotification("support_chat", title, message, supportAgent, null, chatId);
    }

    @Override
    public void notifySupportMessage(User recipient, String senderName, String messagePreview, Integer chatId) {
        String title = "New Support Message";
        String message = senderName + ": " + messagePreview;
        createNotification("support_message", title, message, recipient, null, chatId);
    }
}
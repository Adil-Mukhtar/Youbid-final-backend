// ChatDTO.java
package com.youbid.fyp.DTO;

import java.time.LocalDateTime;

public class ChatDTO {
    private Integer id;
    private UserDTO otherUser;
    private String lastMessage;
    private LocalDateTime lastMessageTime;
    private Integer unreadCount;

    public ChatDTO() {
    }

    public ChatDTO(Integer id, UserDTO otherUser, String lastMessage, LocalDateTime lastMessageTime, Integer unreadCount) {
        this.id = id;
        this.otherUser = otherUser;
        this.lastMessage = lastMessage;
        this.lastMessageTime = lastMessageTime;
        this.unreadCount = unreadCount;
    }

    // Getters and setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public UserDTO getOtherUser() {
        return otherUser;
    }

    public void setOtherUser(UserDTO otherUser) {
        this.otherUser = otherUser;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public LocalDateTime getLastMessageTime() {
        return lastMessageTime;
    }

    public void setLastMessageTime(LocalDateTime lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }

    public Integer getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(Integer unreadCount) {
        this.unreadCount = unreadCount;
    }
}
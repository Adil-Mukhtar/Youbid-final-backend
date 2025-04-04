package com.youbid.fyp.DTO;

import java.time.LocalDateTime;

public class SupportChatDTO {
    private Integer id;
    private UserDTO user;
    private UserDTO supportAgent;
    private String department;
    private String status;
    private String topic;
    private LocalDateTime createdAt;
    private LocalDateTime lastActivityTime;
    private String lastMessage;
    private Integer unreadCount;

    public SupportChatDTO() {
    }

    public SupportChatDTO(Integer id, UserDTO user, UserDTO supportAgent, String department,
                          String status, String topic, LocalDateTime createdAt,
                          LocalDateTime lastActivityTime, String lastMessage, Integer unreadCount) {
        this.id = id;
        this.user = user;
        this.supportAgent = supportAgent;
        this.department = department;
        this.status = status;
        this.topic = topic;
        this.createdAt = createdAt;
        this.lastActivityTime = lastActivityTime;
        this.lastMessage = lastMessage;
        this.unreadCount = unreadCount;
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public UserDTO getUser() {
        return user;
    }

    public void setUser(UserDTO user) {
        this.user = user;
    }

    public UserDTO getSupportAgent() {
        return supportAgent;
    }

    public void setSupportAgent(UserDTO supportAgent) {
        this.supportAgent = supportAgent;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getLastActivityTime() {
        return lastActivityTime;
    }

    public void setLastActivityTime(LocalDateTime lastActivityTime) {
        this.lastActivityTime = lastActivityTime;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public Integer getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(Integer unreadCount) {
        this.unreadCount = unreadCount;
    }
}
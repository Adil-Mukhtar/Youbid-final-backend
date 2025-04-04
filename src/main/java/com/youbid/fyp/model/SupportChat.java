package com.youbid.fyp.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
public class SupportChat {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @ManyToOne
    private User user;

    @ManyToOne
    private User supportAgent;

    private String department;
    private String status; // "open", "closed", "pending"
    private String topic;
    private LocalDateTime createdAt;
    private LocalDateTime lastActivityTime;
    private LocalDateTime closedAt;

    @OneToMany(mappedBy = "supportChat", cascade = CascadeType.ALL)
    @OrderBy("timestamp ASC")
    private List<SupportMessage> messages = new ArrayList<>();

    public SupportChat() {
        this.createdAt = LocalDateTime.now();
        this.lastActivityTime = LocalDateTime.now();
        this.status = "open";
    }

    public SupportChat(User user, String department, String topic) {
        this.user = user;
        this.department = department;
        this.topic = topic;
        this.createdAt = LocalDateTime.now();
        this.lastActivityTime = LocalDateTime.now();
        this.status = "open";
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public User getSupportAgent() {
        return supportAgent;
    }

    public void setSupportAgent(User supportAgent) {
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

    public LocalDateTime getClosedAt() {
        return closedAt;
    }

    public void setClosedAt(LocalDateTime closedAt) {
        this.closedAt = closedAt;
    }

    public List<SupportMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<SupportMessage> messages) {
        this.messages = messages;
    }

    public void addMessage(SupportMessage message) {
        messages.add(message);
        message.setSupportChat(this);
        this.lastActivityTime = message.getTimestamp();
    }

    public void closeChat() {
        this.status = "closed";
        this.closedAt = LocalDateTime.now();
    }
}
package com.youbid.fyp.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class SupportMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "sender_id")
    private User sender;

    @ManyToOne
    @JoinColumn(name = "support_chat_id")
    @JsonIgnore
    private SupportChat supportChat;

    @Column(columnDefinition = "TEXT")
    private String content;

    private LocalDateTime timestamp;
    private boolean isRead;
    private boolean isFromSupport; // To distinguish messages from support vs user

    public SupportMessage() {
        this.timestamp = LocalDateTime.now();
        this.isRead = false;
    }

    public SupportMessage(User sender, String content, boolean isFromSupport) {
        this.sender = sender;
        this.content = content;
        this.timestamp = LocalDateTime.now();
        this.isRead = false;
        this.isFromSupport = isFromSupport;
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public User getSender() {
        return sender;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }

    public SupportChat getSupportChat() {
        return supportChat;
    }

    public void setSupportChat(SupportChat supportChat) {
        this.supportChat = supportChat;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public boolean isFromSupport() {
        return isFromSupport;
    }

    public void setFromSupport(boolean fromSupport) {
        isFromSupport = fromSupport;
    }
}
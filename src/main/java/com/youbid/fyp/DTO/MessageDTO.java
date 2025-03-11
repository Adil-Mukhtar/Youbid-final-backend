// MessageDTO.java
package com.youbid.fyp.DTO;

import java.time.LocalDateTime;

public class MessageDTO {
    private Integer id;
    private Integer senderId;
    private String senderName;
    private String content;
    private LocalDateTime timestamp;
    private boolean isRead;
    private boolean isMine;

    public MessageDTO() {
    }

    public MessageDTO(Integer id, Integer senderId, String senderName, String content, LocalDateTime timestamp, boolean isRead, boolean isMine) {
        this.id = id;
        this.senderId = senderId;
        this.senderName = senderName;
        this.content = content;
        this.timestamp = timestamp;
        this.isRead = isRead;
        this.isMine = isMine;
    }

    // Getters and setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getSenderId() {
        return senderId;
    }

    public void setSenderId(Integer senderId) {
        this.senderId = senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
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

    public boolean isMine() {
        return isMine;
    }

    public void setMine(boolean mine) {
        isMine = mine;
    }
}
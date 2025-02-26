package com.youbid.fyp.DTO;

import java.time.LocalDateTime;

public class MessageDTO {
    private Long id;
    private UserDTO sender;
    private UserDTO receiver;
    private String content;
    private LocalDateTime timestamp;
    private Boolean read;

    public MessageDTO(Long id, UserDTO sender, UserDTO receiver, String content, LocalDateTime timestamp, Boolean read) {
        this.id = id;
        this.sender = sender;
        this.receiver = receiver;
        this.content = content;
        this.timestamp = timestamp;
        this.read = read;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public UserDTO getSender() {
        return sender;
    }

    public UserDTO getReceiver() {
        return receiver;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public Boolean getRead() {
        return read;
    }
}


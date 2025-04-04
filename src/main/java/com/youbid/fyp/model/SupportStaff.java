package com.youbid.fyp.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class SupportStaff {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    private String department;
    private Boolean isAvailable;
    private LocalDateTime lastActiveTime;
    private Integer activeChatsCount;

    public SupportStaff() {
        this.isAvailable = true;
        this.lastActiveTime = LocalDateTime.now();
        this.activeChatsCount = 0;
    }

    public SupportStaff(User user, String department) {
        this.user = user;
        this.department = department;
        this.isAvailable = true;
        this.lastActiveTime = LocalDateTime.now();
        this.activeChatsCount = 0;
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

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public Boolean getIsAvailable() {
        return isAvailable;
    }

    public void setIsAvailable(Boolean available) {
        isAvailable = available;
    }

    public LocalDateTime getLastActiveTime() {
        return lastActiveTime;
    }

    public void setLastActiveTime(LocalDateTime lastActiveTime) {
        this.lastActiveTime = lastActiveTime;
    }

    public Integer getActiveChatsCount() {
        return activeChatsCount;
    }

    public void setActiveChatsCount(Integer activeChatsCount) {
        this.activeChatsCount = activeChatsCount;
    }

    public void incrementActiveChatsCount() {
        this.activeChatsCount++;
    }

    public void decrementActiveChatsCount() {
        if (this.activeChatsCount > 0) {
            this.activeChatsCount--;
        }
    }
}
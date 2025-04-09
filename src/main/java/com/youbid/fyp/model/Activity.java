package com.youbid.fyp.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "admin_activities")
public class Activity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    private String type;  // "user", "listing", "support", etc.
    private String title;
    private String description;
    private LocalDateTime timestamp;

    // Optional related entities
    private Integer userId;     // If the activity is related to a user
    private Integer productId;  // If the activity is related to a product
    private Integer supportId;  // If the activity is related to a support ticket

    public Activity() {
        this.timestamp = LocalDateTime.now();
    }

    public Activity(String type, String title, String description) {
        this.type = type;
        this.title = title;
        this.description = description;
        this.timestamp = LocalDateTime.now();
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public Integer getSupportId() {
        return supportId;
    }

    public void setSupportId(Integer supportId) {
        this.supportId = supportId;
    }
}
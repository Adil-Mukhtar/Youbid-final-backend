package com.youbid.fyp.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    private String name;
    private String description;
    private Integer price;
    private String status;
    private String category;
    private String location;

    @Column(nullable = true)
    private BigDecimal highestBid;

    @ManyToOne
    private User highestBidder;

    @Column
    private LocalDateTime auctionDeadline;

    // Add image paths
    @ElementCollection
    @CollectionTable(name = "product_images", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "image_path")
    private List<String> images = new ArrayList<>();

    @ManyToOne
    private User user;

    private LocalDateTime createdAt;

    public Product() {
    }

    public Product(Integer id, String name, String description, Integer price, String status, String category, String location, List<Bid> bids, BigDecimal highestBid, User highestBidder, LocalDateTime auctionDeadline, User user, LocalDateTime createdAt, List<String> images) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.status = status;
        this.category = category;
        this.location = location;
        this.highestBid = highestBid;
        this.highestBidder = highestBidder;
        this.auctionDeadline = auctionDeadline;
        this.user = user;
        this.createdAt = createdAt;
        this.images = images;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public BigDecimal getHighestBid() {
        return highestBid;
    }

    public void setHighestBid(BigDecimal highestBid) {
        this.highestBid = highestBid;
    }

    public User getHighestBidder() {
        return highestBidder;
    }

    public void setHighestBidder(User highestBidder) {
        this.highestBidder = highestBidder;
    }

    public LocalDateTime getAuctionDeadline() {
        return auctionDeadline;
    }

    public void setAuctionDeadline(LocalDateTime auctionDeadline) {
        this.auctionDeadline = auctionDeadline;
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }

    public void addImage(String imagePath) {
        if (this.images == null) {
            this.images = new ArrayList<>();
        }
        this.images.add(imagePath);
    }
}
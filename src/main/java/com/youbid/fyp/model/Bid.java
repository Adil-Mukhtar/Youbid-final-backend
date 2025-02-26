package com.youbid.fyp.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
public class Bid {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @ManyToOne
    private Product product;


    @ManyToOne
    private User bidder;

    private BigDecimal amount;

    private LocalDateTime bidPlaceTime;


    public Bid() {
        //
    }

    public Bid(Integer id, Product product, User bidder, BigDecimal amount, LocalDateTime bidPlaceTime) {
        this.id = id;
        this.product = product;
        this.bidder = bidder;
        this.amount = amount;
        this.bidPlaceTime = bidPlaceTime;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public User getBidder() {
        return bidder;
    }

    public void setBidder(User bidder) {
        this.bidder = bidder;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDateTime getBidPlaceTime() {
        return bidPlaceTime;
    }

    public void setBidPlaceTime(LocalDateTime bidPlaceTime) {
        this.bidPlaceTime = bidPlaceTime;
    }
}

package com.youbid.fyp.DTO;

import com.youbid.fyp.model.User;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BidDTO {
    private User bidder;
    private BigDecimal amount;
    private LocalDateTime bidPlaceTime;

    public BidDTO() {
        //
    }
    public BidDTO(User bidder, BigDecimal amount, LocalDateTime bidPlaceTime) {
        this.bidder = bidder;
        this.amount = amount;
        this.bidPlaceTime = bidPlaceTime;
    }

    // Getters and setters
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

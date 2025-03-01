package com.youbid.fyp.DTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BidDTO {
    private String bidderName; // ✅ Store only bidder's name
    private BigDecimal amount;
    private LocalDateTime bidPlaceTime;

    public BidDTO() {
        //
    }

    public BidDTO(String bidderName, BigDecimal amount, LocalDateTime bidPlaceTime) {
        this.bidderName = bidderName;
        this.amount = amount;
        this.bidPlaceTime = bidPlaceTime;
    }

    // Getters and setters
    public String getBidderName() {
        return bidderName;
    }

    public void setBidderName(String bidderName) {
        this.bidderName = bidderName;
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

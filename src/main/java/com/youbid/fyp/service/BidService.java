package com.youbid.fyp.service;

import com.youbid.fyp.DTO.BidDTO;
import com.youbid.fyp.model.Bid;
import com.youbid.fyp.model.User;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface BidService {
    Bid placeBid(Integer productId, User bidder, BigDecimal amount) throws Exception;
    List<BidDTO> getBidHistory(Integer productId) throws Exception;
    void processAllAuctionWinnersScheduled();
    Optional<Bid> getHighestBidByProductId(Integer productId);

    // New methods for analytics
    List<Bid> getActiveBidsByUser(Integer userId) throws Exception;
    List<Bid> getLostBidsByUser(Integer userId) throws Exception;
}
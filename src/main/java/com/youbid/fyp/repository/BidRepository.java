package com.youbid.fyp.repository;

import com.youbid.fyp.DTO.BidDTO;
import com.youbid.fyp.model.Bid;
import com.youbid.fyp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BidRepository extends JpaRepository<Bid, Integer> {

    @Query("SELECT b FROM Bid b WHERE b.product.id = :productId ORDER BY b.amount ASC")
    List<Bid> findAllBidsByProductIdOrderByBidAmountAsc(@Param("productId") int productId);

    @Query("SELECT new com.youbid.fyp.DTO.BidDTO(CONCAT(b.bidder.firstname, ' ', b.bidder.lastname), b.amount, b.bidPlaceTime) " +
            "FROM Bid b WHERE b.product.id = :productId ORDER BY b.amount DESC")
    List<BidDTO> findAllBidDetailsByProductIdOrderByBidAmountAscDTO(@Param("productId") Integer productId);

    @Query("SELECT b FROM Bid b WHERE b.product = :product ORDER BY b.amount DESC LIMIT 1")
    Optional<Bid> findTopByProductOrderByAmountDesc(@Param("product") com.youbid.fyp.model.Product product);

    // Find all bids by bidder
    List<Bid> findByBidder(User bidder);

    // Count bids for a product
    @Query("SELECT COUNT(b) FROM Bid b WHERE b.product.id = :productId")
    Integer countBidsByProductId(@Param("productId") Integer productId);

    // Count unique bidders for a product
    @Query("SELECT COUNT(DISTINCT b.bidder.id) FROM Bid b WHERE b.product.id = :productId")
    Integer countUniqueBiddersByProductId(@Param("productId") Integer productId);
}
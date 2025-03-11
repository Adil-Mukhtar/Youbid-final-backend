package com.youbid.fyp.repository;

import com.youbid.fyp.DTO.BidDTO;
import com.youbid.fyp.model.Bid;
import com.youbid.fyp.model.Product;
import com.youbid.fyp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BidRepository extends JpaRepository<Bid, Integer> {

    @Query("SELECT b FROM Bid b WHERE b.product.id = :productId ORDER BY b.amount ASC")
    List<Bid> findAllBidsByProductIdOrderByBidAmountAsc(@Param("productId") int productId);

//    @Query("SELECT b.bidder FROM Bid b WHERE b.product.id = :productId ORDER BY b.amount ASC")
//    List<User> findAllBiddersByProductIdOrderByBidAmountAsc(@Param("productId") int productId);

//    @Query("SELECT new com.youbid.fyp.DTO.BidDTO(b.bidder, b.amount, b.bidPlaceTime) " +
//            "FROM Bid b WHERE b.product.id = :productId ORDER BY b.amount ASC")
//    List<BidDTO> findAllBidDetailsByProductIdOrderByBidAmountAscDTO(@Param("productId") int productId);

    @Query("SELECT new com.youbid.fyp.DTO.BidDTO(CONCAT(b.bidder.firstname, ' ', b.bidder.lastname), b.amount, b.bidPlaceTime) " +
            "FROM Bid b WHERE b.product.id = :productId ORDER BY b.amount DESC")
    List<BidDTO> findAllBidDetailsByProductIdOrderByBidAmountAscDTO(@Param("productId") Integer productId);


    @Query("SELECT b FROM Bid b WHERE b.product = :product ORDER BY b.amount DESC LIMIT 1")
    Optional<Bid> findTopByProductOrderByAmountDesc(@Param("product") com.youbid.fyp.model.Product product);

    // Add this method to your BidRepository.java interface

    List<Bid> findByBidder(User bidder);

}

package com.youbid.fyp.repository;

import com.youbid.fyp.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Integer> {

    @Query("SELECT p FROM Product p WHERE p.user.id = :userId")
    List<Product> findProductByUserId (Integer userId);


    @Query("SELECT p FROM Product p WHERE " +
            "(:query IS NULL OR p.name ILIKE CONCAT('%', :query, '%')) AND " +
            "(:location IS NULL OR p.location ILIKE :location) AND " +
            "(:category IS NULL OR p.category ILIKE :category)")
    List<Product> searchProducts(@Param("query") String query,
                                 @Param("location") String location,
                                 @Param("category") String category);

    //AFTER bidding won
    @Query("SELECT p FROM Product p WHERE p.auctionDeadline < CURRENT_TIMESTAMP AND p.status = 'live'")
    List<Product> findExpiredAuctions();

}

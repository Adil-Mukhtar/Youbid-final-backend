package com.youbid.fyp.repository;

import com.youbid.fyp.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Integer> {

    @Query("SELECT r FROM Review r WHERE r.user.id = :sellerId")
    List<Review> getAllSellerReviewsById (Integer sellerId);
}

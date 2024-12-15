package com.youbid.fyp.service;

import com.youbid.fyp.model.Review;

import java.util.List;

public interface ReviewService {

    Review createReview(Review review, Integer userId, Integer productId) throws Exception;

    Review findReviewById(Integer reviewId) throws Exception;

    List<Review> getAllReviews() throws Exception;

    List<Review> getSellerReviews(Integer sellerId) throws Exception;

    List<Review> getSellerReviewsByProductId(Integer productId) throws Exception;



}

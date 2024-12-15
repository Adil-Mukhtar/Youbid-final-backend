package com.youbid.fyp.controller;


import com.youbid.fyp.model.Review;
import com.youbid.fyp.model.User;
import com.youbid.fyp.service.ProductService;
import com.youbid.fyp.service.ReviewService;
import com.youbid.fyp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class ReviewController {


    @Autowired
    ReviewService reviewService;

    @Autowired
    UserService userService;

    @PostMapping("/review/create/{productId}")
    public ResponseEntity<Review> createReview(@RequestBody Review review,
                                               @RequestHeader("Authorization") String jwt, @PathVariable Integer productId) throws Exception {

        User reqUser = userService.findUserByJwt(jwt);
        Review newReview = reviewService.createReview(review, reqUser.getId(), productId);
        return new ResponseEntity<>(newReview, HttpStatus.CREATED);
    }

    @GetMapping("reviews/getSellerReviews/{sellerId}")
    public ResponseEntity<List<Review>> findSellerReviewsById(@PathVariable Integer sellerId) throws Exception {

        List<Review> reviews = reviewService.getSellerReviews(sellerId);
        return new ResponseEntity<List<Review>>(reviews, HttpStatus.OK);
    }

    @GetMapping("/reviews")
    public ResponseEntity<List<Review>> findAllReviews() throws Exception {
        List<Review> reviews = reviewService.getAllReviews();
        return new ResponseEntity<>(reviews, HttpStatus.OK);
    }

    @GetMapping("review/{reviewId}")
    public ResponseEntity<Review> findReviewById(@PathVariable Integer reviewId) throws Exception {
        Review review = reviewService.findReviewById(reviewId);
        return new ResponseEntity<Review>(review, HttpStatus.OK);
    }

}

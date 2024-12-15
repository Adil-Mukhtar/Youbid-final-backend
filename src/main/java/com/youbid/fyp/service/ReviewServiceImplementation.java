package com.youbid.fyp.service;

import com.youbid.fyp.model.Product;
import com.youbid.fyp.model.Review;
import com.youbid.fyp.model.User;
import com.youbid.fyp.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ReviewServiceImplementation implements ReviewService {

    @Autowired
    ReviewRepository reviewRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private ProductService productService;


    @Override
    public Review createReview(Review review, Integer userId, Integer productId) throws Exception {

        Product selectedProduct = productService.findProductById(productId);
        User seller = selectedProduct.getUser();
        Integer sellerId = seller.getId();

        User reviewPoster = userService.findUserById(userId); //person posting the review

        if(sellerId == reviewPoster.getId())
        {
            throw new Exception("You can't post review on your own product");
        }

        Review newReview = new Review();
        newReview.setUser(seller);
        newReview.setReviewerName(reviewPoster.getFirstname() + " " + reviewPoster.getLastname());
        newReview.setReviewerId(reviewPoster.getId());
        newReview.setContent(review.getContent());
        newReview.setCreatedAt(LocalDateTime.now());

        Review savedReview = reviewRepository.save(newReview);
        System.out.println("Review Posted by user by: " + reviewPoster.getFirstname()+ " " +
                reviewPoster.getLastname() + "having id: " + savedReview.getId());
        return savedReview;
    }

    @Override
    public Review findReviewById(Integer reviewId) throws Exception {

        Optional<Review> opt = reviewRepository.findById(reviewId);

        if(opt.isEmpty()){
            throw new Exception("review not found with id: " + reviewId);
        }
        return opt.get();
    }

    @Override
    public List<Review> getAllReviews() throws Exception {
        return reviewRepository.findAll();
    }

    @Override
    public List<Review> getSellerReviews(Integer sellerId) throws Exception {
        System.out.println(sellerId);
        return reviewRepository.getAllSellerReviewsById(sellerId);
    }

    @Override
    public List<Review> getSellerReviewsByProductId(Integer productId) throws Exception {
        Product selectedProduct = productService.findProductById(productId);
        Integer sellerId = selectedProduct.getUser().getId();
        return reviewRepository.getAllSellerReviewsById(sellerId);
    }

}

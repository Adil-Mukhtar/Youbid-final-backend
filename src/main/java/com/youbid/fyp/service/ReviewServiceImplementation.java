package com.youbid.fyp.service;

import com.youbid.fyp.model.Product;
import com.youbid.fyp.model.Review;
import com.youbid.fyp.model.User;
import com.youbid.fyp.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import java.util.*;
import java.util.stream.Collectors;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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

    //for sentiment

    @Override
    public Map<String, Object> getSentimentAnalysisForReviews(Integer productId) throws Exception {
        // Fetch reviews for the product
        List<Review> reviews = getSellerReviewsByProductId(productId);

        // Extract review contents for sentiment analysis
        List<String> reviewContents = reviews.stream()
                .map(Review::getContent)
                .collect(Collectors.toList());

        if (reviewContents.isEmpty()) {
            throw new Exception("No reviews found for the given product ID");
        }

        // Initialize RestTemplate
        RestTemplate restTemplate = new RestTemplate();

        // Define API URL and headers
        String apiUrl = "http://localhost:8000/classify-reviews";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Prepare the request payload
        Map<String, Object> requestBody = Map.of("reviews", reviewContents);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

        try {
            // Make POST request to Python API
            ResponseEntity<Map> response = restTemplate.exchange(apiUrl, HttpMethod.POST, requestEntity, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                // Process and combine sentiment analysis results with reviews
                @SuppressWarnings("unchecked")
                Map<String, Object> sentimentResults = response.getBody();

                @SuppressWarnings("unchecked")
                List<String> sentiments = (List<String>) sentimentResults.get("individual_classifications");

                List<Map<String, Object>> combinedReviews = new ArrayList<>();
                for (int i = 0; i < reviews.size(); i++) {
                    Review review = reviews.get(i);

                    Map<String, Object> reviewWithSentiment = new HashMap<>();
                    reviewWithSentiment.put("review", review); // Include full review
                    reviewWithSentiment.put("sentiment", sentiments.get(i)); // Add sentiment
                    reviewWithSentiment.put("reviewerName", review.getReviewerName()); // Add reviewer name
                    combinedReviews.add(reviewWithSentiment);
                }

                // Add processed reviews and overall sentiment to the final response
                Map<String, Object> finalResponse = new HashMap<>();
                finalResponse.put("reviews_with_sentiment", combinedReviews);
                finalResponse.put("overall_sentiment", sentimentResults.get("overall_classification"));

                return finalResponse;
            } else {
                throw new Exception("Failed to fetch sentiment analysis: " + response.getStatusCode());
            }
        } catch (Exception e) {
            throw new Exception("Error communicating with sentiment analysis service: " + e.getMessage());
        }
    }


}

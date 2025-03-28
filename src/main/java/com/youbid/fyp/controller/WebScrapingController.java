package com.youbid.fyp.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.HttpEntity;

import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/webscraping")
public class WebScrapingController {

    private final String scrapeApiUrl = "http://localhost:8001"; // Web scraping microservice URL
    private final RestTemplate restTemplate = new RestTemplate();

    @GetMapping("/status")
    public ResponseEntity<Map<String, String>> getStatus() {
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(scrapeApiUrl, Map.class);
            return new ResponseEntity<>(response.getBody(), HttpStatus.OK);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Web scraping service is not available: " + e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    @PostMapping("/product-details")
    public ResponseEntity<?> getProductDetails(@RequestBody Map<String, String> request) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, String>> entity = new HttpEntity<>(request, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    scrapeApiUrl + "/scrape/product",
                    entity,
                    Map.class
            );

            return new ResponseEntity<>(response.getBody(), HttpStatus.OK);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get product details: " + e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/market-info/{category}")
    public ResponseEntity<?> getMarketInfo(@PathVariable String category) {
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(
                    scrapeApiUrl + "/market/info/" + category,
                    Map.class
            );

            return new ResponseEntity<>(response.getBody(), HttpStatus.OK);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get market info: " + e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/popular-categories")
    public ResponseEntity<?> getPopularCategories() {
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(
                    scrapeApiUrl + "/market/popular-categories",
                    Map.class
            );

            return new ResponseEntity<>(response.getBody(), HttpStatus.OK);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get popular categories: " + e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/seasonal-trends")
    public ResponseEntity<?> getSeasonalTrends() {
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(
                    scrapeApiUrl + "/market/seasonal-trends",
                    Map.class
            );

            return new ResponseEntity<>(response.getBody(), HttpStatus.OK);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get seasonal trends: " + e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
package com.youbid.fyp.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;

import java.util.Map;
import java.util.HashMap;

@Service
public class WebScrapingService {

    private final String scrapeApiUrl = "http://localhost:8001"; // Web scraping microservice URL
    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Check if the web scraping service is available
     * @return boolean indicating if the service is available
     */
    public boolean isServiceAvailable() {
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(scrapeApiUrl, Map.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Get product details by scraping web sources
     * @param productName The name of the product to search for
     * @param category The category of the product (optional)
     * @return Map containing the product details
     */
    public Map<String, Object> getProductDetails(String productName, String category) {
        try {
            Map<String, String> request = new HashMap<>();
            request.put("product_name", productName);
            if (category != null && !category.isEmpty()) {
                request.put("category", category);
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, String>> entity = new HttpEntity<>(request, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    scrapeApiUrl + "/scrape/product",
                    entity,
                    Map.class
            );

            return response.getBody();
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to get product details: " + e.getMessage());
            return error;
        }
    }

    /**
     * Get market information for a specific category
     * @param category The category to get market info for
     * @return Map containing the market information
     */
    public Map<String, Object> getMarketInfo(String category) {
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(
                    scrapeApiUrl + "/market/info/" + category,
                    Map.class
            );

            return response.getBody();
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to get market info: " + e.getMessage());
            return error;
        }
    }

    /**
     * Get the most popular categories
     * @return Map containing the popular categories
     */
    public Map<String, Object> getPopularCategories() {
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(
                    scrapeApiUrl + "/market/popular-categories",
                    Map.class
            );

            return response.getBody();
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to get popular categories: " + e.getMessage());
            return error;
        }
    }

    /**
     * Get seasonal trends information
     * @return Map containing the seasonal trends
     */
    public Map<String, Object> getSeasonalTrends() {
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(
                    scrapeApiUrl + "/market/seasonal-trends",
                    Map.class
            );

            return response.getBody();
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to get seasonal trends: " + e.getMessage());
            return error;
        }
    }
}
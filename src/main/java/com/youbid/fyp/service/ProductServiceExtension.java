package com.youbid.fyp.service;

import com.youbid.fyp.model.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductServiceExtension {

    @Autowired
    private com.youbid.fyp.repository.ProductRepository productRepository;

    /**
     * Finds all products with featured listings prioritized
     * @return List of products with featured listings first
     */
    public List<Product> findAllProductsWithFeaturedFirst() {
        List<Product> allProducts = productRepository.findAll();

        // Update product statuses based on expiration dates
        updateProductRewardStatuses(allProducts);

        // Sort products to prioritize featured ones
        return allProducts.stream()
                .sorted(Comparator
                        .comparing((Product p) -> !p.hasFeaturedStatus()) // Featured first
                        .thenComparing(Product::getCreatedAt, Comparator.reverseOrder())) // Then newest
                .collect(Collectors.toList());
    }

    /**
     * Finds all exclusive access products for a specific loyalty tier
     * @param loyaltyTier The loyalty tier to find exclusive products for
     * @return List of exclusive products for the tier
     */
    public List<Product> findExclusiveProducts(String loyaltyTier) {
        List<Product> allProducts = productRepository.findAll();

        // Update statuses first
        updateProductRewardStatuses(allProducts);

        // Filter for exclusive products that match the tier
        return allProducts.stream()
                .filter(Product::hasExclusiveAccess)
                .filter(p -> isEligibleForTier(p.getExclusiveAccessTier(), loyaltyTier))
                .collect(Collectors.toList());
    }

    /**
     * Finds all products with active discounts
     * @return List of products with active discounts
     */
    public List<Product> findDiscountedProducts() {
        List<Product> allProducts = productRepository.findAll();

        // Update statuses first
        updateProductRewardStatuses(allProducts);

        // Filter for products with active discounts
        return allProducts.stream()
                .filter(Product::hasActiveDiscount)
                .collect(Collectors.toList());
    }

    /**
     * Updates the reward statuses of products based on expiration times
     * @param products List of products to update
     */
    @Transactional
    public void updateProductRewardStatuses(List<Product> products) {
        LocalDateTime now = LocalDateTime.now();
        boolean anyUpdates = false;

        for (Product product : products) {
            boolean updated = false;

            // Check featured status expiration
            if (product.getIsFeatured() != null && product.getIsFeatured() &&
                    product.getFeaturedUntil() != null && product.getFeaturedUntil().isBefore(now)) {
                product.setIsFeatured(false);
                updated = true;
            }

            // Check exclusive access expiration
            if (product.getIsExclusive() != null && product.getIsExclusive() &&
                    product.getExclusiveUntil() != null && product.getExclusiveUntil().isBefore(now)) {
                product.setIsExclusive(false);
                updated = true;
            }

            // Check discount expiration
            if (product.getDiscountPercent() != null && product.getDiscountPercent().compareTo(java.math.BigDecimal.ZERO) > 0 &&
                    product.getDiscountUntil() != null && product.getDiscountUntil().isBefore(now)) {
                product.setDiscountPercent(null);
                product.setDiscountCode(null);
                updated = true;
            }

            if (updated) {
                productRepository.save(product);
                anyUpdates = true;
            }
        }
    }

    /**
     * Checks if a user with a given tier is eligible for a product exclusive tier
     * @param productTier The product's required tier
     * @param userTier The user's tier
     * @return True if the user is eligible
     */
    private boolean isEligibleForTier(String productTier, String userTier) {
        if (productTier == null || userTier == null) {
            return false;
        }

        // Define tier hierarchy (higher index = higher tier)
        String[] tiers = {"Bronze", "Silver", "Gold", "Platinum"};

        int userTierIndex = -1;
        int productTierIndex = -1;

        // Find indices for the tiers
        for (int i = 0; i < tiers.length; i++) {
            if (tiers[i].equalsIgnoreCase(userTier)) {
                userTierIndex = i;
            }
            if (tiers[i].equalsIgnoreCase(productTier)) {
                productTierIndex = i;
            }
        }

        // If either tier is not found, assume not eligible
        if (userTierIndex == -1 || productTierIndex == -1) {
            return false;
        }

        // User is eligible if their tier is equal to or higher than the required tier
        return userTierIndex >= productTierIndex;
    }
}
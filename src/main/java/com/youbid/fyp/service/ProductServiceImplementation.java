package com.youbid.fyp.service;


import com.youbid.fyp.model.Bid;
import com.youbid.fyp.model.Product;
import com.youbid.fyp.model.ProductStatus;
import com.youbid.fyp.model.User;
import com.youbid.fyp.repository.BidRepository;
import com.youbid.fyp.repository.ProductRepository;
import com.youbid.fyp.repository.UserRepository;
import org.hibernate.sql.ast.tree.expression.Over;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductServiceImplementation implements ProductService {

    @Autowired
    ProductRepository productRepository;

    @Autowired
    UserService userService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    BidRepository bidRepository;

    @Autowired
    CategoryService categoryService;

    @Autowired
    ProductStatusService productStatusService;

    @Autowired
    FileStorageService fileStorageService;

    @Override
    public Product createProduct(Product product, Integer userId) throws Exception {
        ProductStatus productStatus = productStatusService.getProductStatusById(1);

        User user = userService.findUserById(userId);
        Product newProduct = new Product();
        newProduct.setName(product.getName());
        newProduct.setPrice(product.getPrice());
        newProduct.setDescription(product.getDescription());
        newProduct.setStatus(productStatus.getStatus());
        newProduct.setUser(user);
        newProduct.setCreatedAt(LocalDateTime.now()); // Current time from system
        newProduct.setLocation(product.getLocation());// Set the location
        newProduct.setCategory(product.getCategory());
        newProduct.setAuctionDeadline(product.getAuctionDeadline());

        // Set images if provided
        if (product.getImages() != null && !product.getImages().isEmpty()) {
            newProduct.setImages(product.getImages());
        }

        return productRepository.save(newProduct);
    }

    @Override
    public Product createProductWithImages(Product product, List<MultipartFile> images, Integer userId) throws Exception {
        ProductStatus productStatus = productStatusService.getProductStatusById(1);

        User user = userService.findUserById(userId);
        Product newProduct = new Product();
        newProduct.setName(product.getName());
        newProduct.setPrice(product.getPrice());
        newProduct.setDescription(product.getDescription());
        newProduct.setStatus(productStatus.getStatus());
        newProduct.setUser(user);
        newProduct.setCreatedAt(LocalDateTime.now());
        newProduct.setLocation(product.getLocation());
        newProduct.setCategory(product.getCategory());
        newProduct.setAuctionDeadline(product.getAuctionDeadline());

        // Save the product first to get an ID
        Product savedProduct = productRepository.save(newProduct);

        // Process and store images
        if (images != null && !images.isEmpty()) {
            List<String> imageNames = fileStorageService.storeFiles(images);
            savedProduct.setImages(imageNames);
            savedProduct = productRepository.save(savedProduct);
        }

        return savedProduct;
    }

    @Override
    public String deleteProduct(Integer id, Integer userId) throws Exception {
        Product product = findProductById(id);
        User user = userService.findUserById(userId);
        if(product.getUser().getId() != user.getId()){
            throw new Exception("You can't delete someone else's product");
        }

        // Delete associated image files
        if (product.getImages() != null && !product.getImages().isEmpty()) {
            for (String imagePath : product.getImages()) {
                fileStorageService.deleteFile(imagePath);
            }
        }

        productRepository.delete(product);
        return "Product deleted successfully!";
    }

    @Override
    public Product updateProduct(Product product, Integer userId, Integer productId) throws Exception {
        Product oldProduct = findProductById(productId);
        if(oldProduct.getId() == null) {
            throw new Exception("Product not found with id: " + product.getId());
        }

        User user = userService.findUserById(userId);

        if(product.getName() != null){
            oldProduct.setName(product.getName());
        }
        if(product.getPrice() != null){
            oldProduct.setPrice(product.getPrice());
        }
        if(product.getDescription() != null){
            oldProduct.setDescription(product.getDescription());
        }
        if(product.getLocation() != null){
            oldProduct.setLocation(product.getLocation());
        }
        if(product.getCategory() != null){
            oldProduct.setCategory(product.getCategory());
        }
        if(product.getStatus() != null && product.getStatus() != "live"){
            oldProduct.setStatus(product.getStatus());
        }
        if(product.getAuctionDeadline() != null){
            oldProduct.setAuctionDeadline(product.getAuctionDeadline());
        }

        // Update images if provided
        if (product.getImages() != null && !product.getImages().isEmpty()) {
            oldProduct.setImages(product.getImages());
        }

        Product updatedProduct = productRepository.save(oldProduct);

        return updatedProduct;
    }

    @Override
    public Product updateProductWithImages(Product product, List<MultipartFile> newImages, Integer userId, Integer productId) throws Exception {
        Product oldProduct = findProductById(productId);
        if(oldProduct.getId() == null) {
            throw new Exception("Product not found with id: " + product.getId());
        }

        User user = userService.findUserById(userId);

        if(product.getName() != null){
            oldProduct.setName(product.getName());
        }
        if(product.getPrice() != null){
            oldProduct.setPrice(product.getPrice());
        }
        if(product.getDescription() != null){
            oldProduct.setDescription(product.getDescription());
        }
        if(product.getLocation() != null){
            oldProduct.setLocation(product.getLocation());
        }
        if(product.getCategory() != null){
            oldProduct.setCategory(product.getCategory());
        }
        if(product.getStatus() != null && product.getStatus() != "live"){
            oldProduct.setStatus(product.getStatus());
        }
        if(product.getAuctionDeadline() != null){
            oldProduct.setAuctionDeadline(product.getAuctionDeadline());
        }

        // If we have new image uploads, process them
        if (newImages != null && !newImages.isEmpty()) {
            // Delete old images if requested
            if (product.getImages() == null || product.getImages().isEmpty()) {
                // If product.getImages is empty, it means we're replacing all images
                if (oldProduct.getImages() != null) {
                    for (String imagePath : oldProduct.getImages()) {
                        fileStorageService.deleteFile(imagePath);
                    }
                }

                // Store new images
                List<String> imageNames = fileStorageService.storeFiles(newImages);
                oldProduct.setImages(imageNames);
            } else {
                // We're adding to existing images
                List<String> imageNames = fileStorageService.storeFiles(newImages);
                List<String> updatedImages = new ArrayList<>(oldProduct.getImages());
                updatedImages.addAll(imageNames);
                oldProduct.setImages(updatedImages);
            }
        } else if (product.getImages() != null) {
            // If no new uploads but images list is provided, use that list
            // (This handles image deletion from the client side)

            // Find images to delete (images in oldProduct but not in product.getImages)
            if (oldProduct.getImages() != null) {
                List<String> imagesToDelete = oldProduct.getImages().stream()
                        .filter(img -> !product.getImages().contains(img))
                        .collect(Collectors.toList());

                // Delete files
                for (String imagePath : imagesToDelete) {
                    fileStorageService.deleteFile(imagePath);
                }
            }

            oldProduct.setImages(product.getImages());
        }

        return productRepository.save(oldProduct);
    }

    @Override
    public List<Product> findProductsByUserId(Integer userId) {
        System.out.println(userId);
        return productRepository.findProductByUserId(userId);
    }

    @Override
    public Product findProductById(Integer productId) throws Exception {

        Optional<Product> opt = productRepository.findById(productId);

        if(opt.isEmpty()){
            throw new Exception("Product not found with id: " + productId);
        }
        return opt.get();
    }

    @Override
    public List<Product> findAllProducts() {

        return productRepository.findAll();
    }


    //for admin

    @Override
    public Product updateProductbyAdmin(Product product, Integer productId) throws Exception {

        Product oldProduct = findProductById(productId);
        if(oldProduct.getId() == null) {
            throw new Exception("Product not found with id: " + product.getId());
        }

        if(product.getName() != null){
            oldProduct.setName(product.getName());
        }
        if(product.getPrice() != null){
            oldProduct.setPrice(product.getPrice());
        }
        if(product.getDescription() != null){
            oldProduct.setDescription(product.getDescription());
        }
        if(product.getStatus() != null){
            oldProduct.setStatus(product.getStatus());
        }
        if(product.getLocation() != null){
            oldProduct.setLocation(product.getLocation());
        }
        if(product.getCategory() != null){
            oldProduct.setCategory(product.getCategory());
        }
        if(product.getAuctionDeadline() != null){
            oldProduct.setAuctionDeadline(product.getAuctionDeadline());
        }

        // Update images if provided
        if (product.getImages() != null) {
            oldProduct.setImages(product.getImages());
        }

        Product updatedProduct = productRepository.save(oldProduct);

        return updatedProduct;
    }

    //for admin
    @Override
    public String deleteProductByAdmin(Integer productId) throws Exception {
        Product product = findProductById(productId);

        // Delete associated image files
        if (product.getImages() != null && !product.getImages().isEmpty()) {
            for (String imagePath : product.getImages()) {
                fileStorageService.deleteFile(imagePath);
            }
        }

        productRepository.delete(product);
        return "Product deleted successfully!";
    }

    @Override
    public List<Product> searchProducts(String query, String location, String category, Double minPrice, Double maxPrice) {
        if (query == null && location == null && category == null && minPrice == null && maxPrice == null) {
            return productRepository.findAll();
        }
        return productRepository.searchProducts(query, location, category, minPrice, maxPrice);
    }
}
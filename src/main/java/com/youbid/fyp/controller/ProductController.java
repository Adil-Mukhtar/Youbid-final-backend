package com.youbid.fyp.controller;

import com.youbid.fyp.model.Product;
import com.youbid.fyp.model.User;
import com.youbid.fyp.response.ApiResponse;
import com.youbid.fyp.service.ActivityService;
import com.youbid.fyp.service.ProductService;
import com.youbid.fyp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
public class ProductController {

    @Autowired
    ProductService productService;

    @Autowired
    UserService userService;

    @Autowired
    ActivityService activityService;

    @PostMapping("/products/create")
    public ResponseEntity<Product> createProduct(@RequestBody Product product,
                                                 @RequestHeader("Authorization") String jwt) throws Exception {

        User reqUser = userService.findUserByJwt(jwt);
        Product createdProduct = productService.createProduct(product, reqUser.getId());

        // Track activity
        activityService.trackProductActivity(
                "New Listing Created",
                String.format("%s created listing \"%s\"", reqUser.getFirstname(), product.getName()),
                createdProduct.getId()
        );

        return new ResponseEntity<>(createdProduct, HttpStatus.CREATED);
    }

    @PostMapping(value = "/products/create-with-images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Product> createProductWithImages(
            @RequestPart("product") Product product,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            @RequestHeader("Authorization") String jwt) throws Exception {

        User reqUser = userService.findUserByJwt(jwt);
        Product createdProduct = productService.createProductWithImages(product, images, reqUser.getId());

        // Track activity
        activityService.trackProductActivity(
                "New Listing Created with Images",
                String.format("%s created listing \"%s\" with %d images",
                        reqUser.getFirstname(), product.getName(),
                        images != null ? images.size() : 0),
                createdProduct.getId()
        );

        return new ResponseEntity<>(createdProduct, HttpStatus.CREATED);
    }

    @PutMapping("/products/user/update/{productId}")
    public ResponseEntity<Product> updateProduct(@PathVariable Integer productId, @RequestHeader("Authorization") String jwt, @RequestBody Product product) throws Exception {

        User reqUser = userService.findUserByJwt(jwt);
        Product updatedProduct = productService.updateProduct(product,reqUser.getId(), productId);

        // Track activity
        activityService.trackProductActivity(
                "Listing Updated",
                String.format("%s updated listing \"%s\"", reqUser.getFirstname(), updatedProduct.getName()),
                updatedProduct.getId()
        );

        return new ResponseEntity<>(updatedProduct, HttpStatus.OK);
    }

    @PutMapping(value = "/products/user/update-with-images/{productId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Product> updateProductWithImages(
            @PathVariable Integer productId,
            @RequestPart("product") Product product,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            @RequestHeader("Authorization") String jwt) throws Exception {

        User reqUser = userService.findUserByJwt(jwt);
        Product updatedProduct = productService.updateProductWithImages(product, images, reqUser.getId(), productId);

        // Track activity
        activityService.trackProductActivity(
                "Listing Updated with Images",
                String.format("%s updated listing \"%s\" with images", reqUser.getFirstname(), updatedProduct.getName()),
                updatedProduct.getId()
        );

        return new ResponseEntity<>(updatedProduct, HttpStatus.OK);
    }

    @DeleteMapping("/products/user/delete/{productId}")
    public ResponseEntity<ApiResponse> deleteProduct(@PathVariable Integer productId, @RequestHeader("Authorization") String jwt ) throws Exception {

        User reqUser = userService.findUserByJwt(jwt);
        Product product = productService.findProductById(productId);

        String message = productService.deleteProduct(productId, reqUser.getId());

        // Track activity
        activityService.trackProductActivity(
                "Listing Deleted",
                String.format("%s deleted listing \"%s\"", reqUser.getFirstname(), product.getName()),
                productId
        );

        ApiResponse res = new ApiResponse();
        res.setMessage(message);
        return new ResponseEntity<ApiResponse>(res, HttpStatus.OK);
    }

    @GetMapping("/products/user")
    public ResponseEntity<List<Product>> findUsersProduct(@RequestHeader("Authorization") String jwt) {

        User reqUser = userService.findUserByJwt(jwt);
        List<Product> products = productService.findProductsByUserId(reqUser.getId());
        return new ResponseEntity<List<Product>>(products, HttpStatus.OK);
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<Product> findProductByIdHandler(@PathVariable Integer productId) throws Exception {
        Product product = productService.findProductById(productId);
        return new ResponseEntity<Product>(product, HttpStatus.ACCEPTED);
    }

    @GetMapping("/products")
    public ResponseEntity<List<Product>> getAllProducts(){
        List<Product> products = productService.findAllProducts();
        return new ResponseEntity<List<Product>>(products, HttpStatus.OK);
    }
}
package com.youbid.fyp.controller;

import com.youbid.fyp.enums.City;
import com.youbid.fyp.model.Product;
import com.youbid.fyp.model.User;
import com.youbid.fyp.response.ApiResponse;
import com.youbid.fyp.service.ProductService;
import com.youbid.fyp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class ProductController {

    @Autowired
    ProductService productService;

    @Autowired
    UserService userService;


    @PostMapping("/products/create")
    public ResponseEntity<Product> createProduct(@RequestBody Product product,
                                                 @RequestHeader("Authorization") String jwt) throws Exception {

        User reqUser = userService.findUserByJwt(jwt);
        Product createdProduct = productService.createProduct(product, reqUser.getId());
        return new ResponseEntity<>(createdProduct, HttpStatus.CREATED);
    }

    @PutMapping("/products/user/update/{productId}")
    public ResponseEntity<Product> updateProduct(@PathVariable Integer productId, @RequestHeader("Authorization") String jwt, @RequestBody Product product) throws Exception {

        User reqUser = userService.findUserByJwt(jwt);
        Product updatedProduct = productService.updateProduct(product,reqUser.getId(), productId);
        return new ResponseEntity<>(updatedProduct, HttpStatus.OK);
    }

    @DeleteMapping("/products/user/delete/{productId}")
    public ResponseEntity<ApiResponse> deleteProduct(@PathVariable Integer productId, @RequestHeader("Authorization") String jwt ) throws Exception {

        User reqUser = userService.findUserByJwt(jwt);

        String message = productService.deleteProduct(productId, reqUser.getId());
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

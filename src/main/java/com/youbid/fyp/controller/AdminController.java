package com.youbid.fyp.controller;



import com.youbid.fyp.config.JwtProvider;
import com.youbid.fyp.model.ProductStatus;
import com.youbid.fyp.model.User;
import com.youbid.fyp.model.Product;
import com.youbid.fyp.repository.UserRepository;
import com.youbid.fyp.response.ApiResponse;
import com.youbid.fyp.response.AuthResponse;
import com.youbid.fyp.service.ProductStatusService;
import com.youbid.fyp.service.ReviewService;
import com.youbid.fyp.service.UserService;
import com.youbid.fyp.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {


    @Autowired
    private UserService userService;

    @Autowired
    private ProductService productService;

    @Autowired
    UserRepository userRepository;


    @Autowired
    ProductStatusService productStatusService;

    @Autowired
    private PasswordEncoder passwordEncoder;


    @GetMapping("/dashboard")
    public String dashboard() {
        return "\nThis is Admin Dashboard!\n";
    }

    @GetMapping("/allUsers")
    public List<User> getUsers(){
        List<User> users = userRepository.findAll();
        return users;
    }

    @GetMapping("/searchUser")
    public List<User> searchUser(@RequestParam("query")String query){
        List<User> users = userService.searchUser(query);
        return users;
    }

    @PutMapping("/updateUser/{userId}")
    public User updateUserById(@RequestBody User user, @PathVariable Integer userId) throws Exception{

        User updatedUser = userService.updateUser(user, userId);
        return updatedUser;
    }

    @GetMapping("/getUserById/{userId}")
    public User getUserById(@PathVariable("userId") Integer id) throws Exception{
        User user = userRepository.findById(id).get();
        return user;
    }

    @GetMapping("/productById/{productId}")
    public ResponseEntity<Product> findProductByIdHandler(@PathVariable Integer productId) throws Exception {
        Product product = productService.findProductById(productId);
        return new ResponseEntity<Product>(product, HttpStatus.ACCEPTED);
    }

    @GetMapping("/allProducts")
    public ResponseEntity<List<Product>> getAllProducts(){
        List<Product> products = productService.findAllProducts();
        return new ResponseEntity<List<Product>>(products, HttpStatus.OK);
    }

    @PostMapping("/register")
    public AuthResponse createAdmin(@RequestBody User user) throws Exception {
        User isExist = userRepository.findByEmail(user.getEmail());

        if (isExist != null) {
            throw new Exception("Email already in use by another account!");
        }

        User newUser = new User();

        newUser.setEmail(user.getEmail());
        newUser.setPassword(passwordEncoder.encode(user.getPassword()));
        newUser.setFirstname(user.getFirstname());
        newUser.setLastname(user.getLastname());
        newUser.setRole("ADMIN");
        newUser.setGender(user.getGender());
        newUser.setBalance(user.getBalance());
        newUser.setCellphone(user.getCellphone());
        newUser.setBanned(false);
        newUser.setSuspended(false);
        newUser.setSuspensionDate(null);

        User savedUser = userRepository.save(newUser);
        Authentication authentication = new UsernamePasswordAuthenticationToken(user.getEmail(), user.getPassword());

        String token = JwtProvider.generateToken(authentication, savedUser);
        AuthResponse res = new AuthResponse(token, "Admin Registered Successfully! :)", savedUser);

        return res;
    }

    @PutMapping("/products/update/{productId}")
    public ResponseEntity<Product> updateProductAdmin(@PathVariable Integer productId, @RequestBody Product product) throws Exception {
        Product productfound = productService.findProductById(productId);
        Product updatedProduct = productService.updateProductbyAdmin(product, productId);
        return new ResponseEntity<>(updatedProduct, HttpStatus.OK);
    }

    @DeleteMapping("/product/delete/{productId}")
    public ResponseEntity<ApiResponse> deleteProduct(@PathVariable Integer productId) throws Exception {

        String message = productService.deleteProductByAdmin(productId);
        ApiResponse res = new ApiResponse();
        res.setMessage(message);
        return new ResponseEntity<ApiResponse>(res, HttpStatus.OK);
    }

    //register new admin
    @PostMapping("/registerNewAdmin")
    public AuthResponse registerNewAdmin(@RequestBody User user) throws Exception {
        User isExist = userRepository.findByEmail(user.getEmail());

        if (isExist != null) {
            throw new Exception("Email already in use by another admin!");
        }

        User newUser = new User();

        newUser.setEmail(user.getEmail());
        newUser.setPassword(passwordEncoder.encode(user.getPassword()));
        newUser.setFirstname(user.getFirstname());
        newUser.setLastname(user.getLastname());
        newUser.setRole("ADMIN");
        newUser.setGender(user.getGender());
        newUser.setBalance(user.getBalance());
        newUser.setCellphone(user.getCellphone());
        newUser.setBanned(false);
        newUser.setSuspended(false);
        newUser.setSuspensionDate(null);

        User savedUser = userRepository.save(newUser);
        Authentication authentication = new UsernamePasswordAuthenticationToken(user.getEmail(), user.getPassword());

        String token = JwtProvider.generateToken(authentication, savedUser);
        AuthResponse res = new AuthResponse(token, "New Admin Registered Successfully! :)", savedUser);

        return res;
    }


    //for product status

    @PutMapping("/productStatus/update")
    public ResponseEntity<ProductStatus> updateProductStatus(@RequestBody ProductStatus productStatus) throws Exception {

        ProductStatus foundProductStatus = productStatusService.getProductStatusById(1);
        foundProductStatus = productStatusService.updateProductStatus(productStatus.getStatus());
        return new ResponseEntity<>(foundProductStatus, HttpStatus.OK);
    }

    @PostMapping("/productStatus/create")
    public ResponseEntity<ProductStatus> newProductStatus() throws Exception {

        ProductStatus productStatus = new ProductStatus();
        productStatus = productStatusService.createProductStatus();
        return new ResponseEntity<>(productStatus, HttpStatus.OK);
    }

    @GetMapping("/productStatus")
    public ResponseEntity<ProductStatus> getProductStatus() throws Exception {
        ProductStatus productStatus = productStatusService.getProductStatusById(1);
        return new ResponseEntity<>(productStatus, HttpStatus.OK);
    }

// src/main/java/com/youbid/fyp/controller/AdminController.java
// (Add this method to the existing AdminController class)

    @PostMapping("/users/{userId}/upload-profile-picture")
    public ResponseEntity<?> uploadUserProfilePicture(@PathVariable Integer userId, @RequestParam("file") MultipartFile file) {
        try {
            User user = userService.updateProfilePicture(userId, file);
            return ResponseEntity.ok(Map.of(
                    "message", "Profile picture uploaded successfully",
                    "profilePicture", user.getProfilePicture()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to upload profile picture: " + e.getMessage()));
        }
    }

}
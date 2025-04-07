package com.youbid.fyp.controller;



import com.youbid.fyp.config.JwtProvider;
import com.youbid.fyp.model.*;
import com.youbid.fyp.repository.LoyaltyPointsRepository;
import com.youbid.fyp.repository.RewardRepository;
import com.youbid.fyp.repository.UserRepository;
import com.youbid.fyp.repository.UserRewardRepository;
import com.youbid.fyp.response.ApiResponse;
import com.youbid.fyp.response.AuthResponse;
import com.youbid.fyp.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
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
    private LoyaltyService loyaltyService;

    @Autowired
    private RewardService rewardService;

    @Autowired
    private LoyaltyPointsRepository loyaltyPointsRepository;

    @Autowired
    private RewardRepository rewardRepository;

    @Autowired
    private UserRewardRepository userRewardRepository;


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

    // Update in AdminController.java
// Add this method to register support users

    @PostMapping("/registerSupportUser")
    public AuthResponse registerSupportUser(@RequestBody User user) throws Exception {
        User isExist = userRepository.findByEmail(user.getEmail());

        if (isExist != null) {
            throw new Exception("Email already in use by another account!");
        }

        User newUser = new User();

        newUser.setEmail(user.getEmail());
        newUser.setPassword(passwordEncoder.encode(user.getPassword()));
        newUser.setFirstname(user.getFirstname());
        newUser.setLastname(user.getLastname());
        newUser.setRole("SUPPORT");
        newUser.setGender(user.getGender());
        newUser.setBalance(user.getBalance());
        newUser.setCellphone(user.getCellphone());
        newUser.setBanned(false);
        newUser.setSuspended(false);
        newUser.setSuspensionDate(null);

        User savedUser = userRepository.save(newUser);
        Authentication authentication = new UsernamePasswordAuthenticationToken(user.getEmail(), user.getPassword());

        String token = JwtProvider.generateToken(authentication, savedUser);
        AuthResponse res = new AuthResponse(token, "Support User Registered Successfully! :)", savedUser);

        return res;
    }


    /**
     * Get analytics about the loyalty program
     */
    @GetMapping("/loyalty/analytics")
    public ResponseEntity<?> getLoyaltyAnalytics() {
        try {
            // Get all users
            List<User> users = userRepository.findAll();

            // Calculate total points in the system
            int totalPointsIssued = 0;
            int totalPointsRedeemed = 0;

            // Count users in each tier
            int bronzeUsers = 0;
            int silverUsers = 0;
            int goldUsers = 0;
            int platinumUsers = 0;

            for (User user : users) {
                Integer userPoints = loyaltyService.getUserPointsBalance(user.getId());

                // Update tier counts
                if (userPoints >= 1000) {
                    platinumUsers++;
                } else if (userPoints >= 500) {
                    goldUsers++;
                } else if (userPoints >= 100) {
                    silverUsers++;
                } else {
                    bronzeUsers++;
                }
            }

            // Get all point transactions
            List<LoyaltyPoints> allTransactions = loyaltyPointsRepository.findAll();

            // Calculate total points issued and redeemed
            for (LoyaltyPoints transaction : allTransactions) {
                if ("EARNED".equals(transaction.getTransactionType())) {
                    totalPointsIssued += transaction.getPoints();
                } else if ("REDEEMED".equals(transaction.getTransactionType())) {
                    totalPointsRedeemed += transaction.getPoints();
                }
            }

            // Get popular rewards
            List<Reward> rewards = rewardRepository.findAll();
            List<UserReward> redeemedRewards = userRewardRepository.findAll();

            Map<Integer, Integer> rewardRedemptionCounts = new HashMap<>();
            for (UserReward userReward : redeemedRewards) {
                Integer rewardId = userReward.getReward().getId();
                rewardRedemptionCounts.put(rewardId, rewardRedemptionCounts.getOrDefault(rewardId, 0) + 1);
            }

            // Create response
            Map<String, Object> response = new HashMap<>();
            response.put("totalUsers", users.size());
            response.put("totalPointsIssued", totalPointsIssued);
            response.put("totalPointsRedeemed", totalPointsRedeemed);
            response.put("totalPointsActive", totalPointsIssued - totalPointsRedeemed);

            Map<String, Integer> tierCounts = new HashMap<>();
            tierCounts.put("bronze", bronzeUsers);
            tierCounts.put("silver", silverUsers);
            tierCounts.put("gold", goldUsers);
            tierCounts.put("platinum", platinumUsers);
            response.put("tierCounts", tierCounts);

            response.put("rewardRedemptionCounts", rewardRedemptionCounts);

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(Map.of("error", e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get loyalty status for a specific user
     */
    @GetMapping("/loyalty/user/{userId}")
    public ResponseEntity<?> getUserLoyaltyStatus(@PathVariable Integer userId) {
        try {
            Map<String, Object> status = loyaltyService.getUserLoyaltyStatus(userId);
            return new ResponseEntity<>(status, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(Map.of("error", e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Award loyalty points to a user
     */
    @PostMapping("/loyalty/award")
    public ResponseEntity<?> awardLoyaltyPoints(@RequestBody Map<String, Object> request) {
        try {
            Integer userId = (Integer) request.get("userId");
            Integer points = (Integer) request.get("points");
            String source = (String) request.get("source");
            String description = (String) request.get("description");
            Integer productId = (Integer) request.get("productId");

            LoyaltyPoints result = loyaltyService.awardPoints(
                    userId, points, source, description, productId);

            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(Map.of("error", e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Create a new reward
     */
    @PostMapping("/loyalty/rewards/create")
    public ResponseEntity<?> createReward(@RequestBody Reward reward) {
        try {
            Reward newReward = rewardService.createReward(reward);
            return new ResponseEntity<>(newReward, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(Map.of("error", e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Update an existing reward
     */
    @PutMapping("/loyalty/rewards/update/{rewardId}")
    public ResponseEntity<?> updateReward(@PathVariable Integer rewardId, @RequestBody Reward reward) {
        try {
            Reward updatedReward = rewardService.updateReward(reward, rewardId);
            return new ResponseEntity<>(updatedReward, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(Map.of("error", e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Deactivate a reward
     */
    @PutMapping("/loyalty/rewards/deactivate/{rewardId}")
    public ResponseEntity<?> deactivateReward(@PathVariable Integer rewardId) {
        try {
            Reward deactivatedReward = rewardService.deactivateReward(rewardId);
            return new ResponseEntity<>(deactivatedReward, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(Map.of("error", e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

//    @PutMapping("/loyalty/settings")
//    public ResponseEntity<?> updateLoyaltySettings(@RequestBody Map<String, Object> settings,
//                                                   @RequestHeader("Authorization") String jwt) {
//        try {
//            User admin = userService.findUserByJwt(jwt);
//            if (!"ADMIN".equals(admin.getRole())) {
//                return ResponseEntity.status(HttpStatus.FORBIDDEN)
//                        .body(Map.of("error", "Access denied"));
//            }
//
//            Map<String, Object> result = loyaltyService.updateLoyaltySettings(settings);
//            return new ResponseEntity<>(result, HttpStatus.OK);
//        } catch (Exception e) {
//            return new ResponseEntity<>(Map.of("error", e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }
//
//    @GetMapping("/loyalty/settings")
//    public ResponseEntity<?> getLoyaltySettings(@RequestHeader("Authorization") String jwt) {
//        try {
//            User admin = userService.findUserByJwt(jwt);
//            if (!"ADMIN".equals(admin.getRole())) {
//                return ResponseEntity.status(HttpStatus.FORBIDDEN)
//                        .body(Map.of("error", "Access denied"));
//            }
//
//            Map<String, Object> settings = loyaltyService.getLoyaltySettings();
//            return new ResponseEntity<>(settings, HttpStatus.OK);
//        } catch (Exception e) {
//            return new ResponseEntity<>(Map.of("error", e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }

    // Add these endpoints to your existing AdminController.java file

    /**
     * Get loyalty program settings
     */
    @GetMapping("/loyalty/settings")
    public ResponseEntity<?> getLoyaltySettings() {
        try {
            Map<String, Object> settings = loyaltyService.getLoyaltySettings();
            return new ResponseEntity<>(settings, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(Map.of("error", e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Update loyalty program settings
     */
    @PutMapping("/loyalty/settings")
    public ResponseEntity<?> updateLoyaltySettings(@RequestBody Map<String, Object> settings) {
        try {
            System.out.println("Received settings update: " + settings);
            Map<String, Object> result = loyaltyService.updateLoyaltySettings(settings);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception e) {
            System.err.println("Error updating loyalty settings: " + e.getMessage());
            e.printStackTrace();
            return new ResponseEntity<>(Map.of("error", e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}


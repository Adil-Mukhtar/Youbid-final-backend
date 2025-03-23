// src/main/java/com/youbid/fyp/controller/UserController.java
// Add endpoint for uploading profile picture

package com.youbid.fyp.controller;

import com.youbid.fyp.model.Product;
import com.youbid.fyp.model.User;
import com.youbid.fyp.repository.ProductRepository;
import com.youbid.fyp.repository.UserRepository;
import com.youbid.fyp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
public class UserController {

    @Autowired
    UserRepository userRepository;

    @Autowired
    UserService userService;

    @GetMapping("/api/users")
    public List<User> getUsers(){
        List<User> users = userRepository.findAll();
        return users;
    }

    @GetMapping("/api/users/{userId}")
    public User getUserById(@PathVariable("userId") Integer id) throws Exception{
        User user = userRepository.findById(id).get();
        return user;
    }

    @PutMapping("/api/users")
    public User updateUser(@RequestBody User user, @RequestHeader("Authorization") String jwt) throws Exception{
        User reqUser = userService.findUserByJwt(jwt);
        User updatedUser = userService.updateUser(user, reqUser.getId());
        return updatedUser;
    }

    @GetMapping("/api/users/search")
    public List<User> searchUser(@RequestParam("query")String query){
        List<User> users = userService.searchUser(query);
        return users;
    }

    @GetMapping("/api/users/profile")
    public User getUserFromToken(@RequestHeader("Authorization") String jwt){
        User user = userService.findUserByJwt(jwt);
        user.setPassword(null); // we don't want to send password in profile
        return user;
    }

    @GetMapping("/api/user/wonItems")
    public List<Product> getUserWonItems(@RequestHeader("Authorization") String jwt){
        User user = userService.findUserByJwt(jwt);
        return user.getWonItems();
    }

    @PostMapping("/api/users/upload-profile-picture")
    public ResponseEntity<?> uploadProfilePicture(
            @RequestParam("file") MultipartFile file,
            @RequestHeader("Authorization") String jwt) {
        try {
            User user = userService.findUserByJwt(jwt);
            User updatedUser = userService.updateProfilePicture(user.getId(), file);
            return ResponseEntity.ok(Map.of(
                    "message", "Profile picture uploaded successfully",
                    "profilePicture", updatedUser.getProfilePicture()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to upload profile picture: " + e.getMessage()));
        }
    }
}
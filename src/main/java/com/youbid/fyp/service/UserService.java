// src/main/java/com/youbid/fyp/service/UserService.java
// Add method for updating profile picture

package com.youbid.fyp.service;

import com.youbid.fyp.model.Product;
import com.youbid.fyp.model.User;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UserService {
    public User registerUser(User user);
    public User findUserById(Integer userId) throws Exception;
    public User findUserByEmail(String email);
    public User updateUser(User user, Integer userId) throws Exception;
    public List<User> searchUser(String query);
    public User findUserByJwt(String jwt);
    public User updateProfilePicture(Integer userId, MultipartFile file) throws Exception;
}
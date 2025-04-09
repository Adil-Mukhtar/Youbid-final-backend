// src/main/java/com/youbid/fyp/service/UserServiceImplementation.java
// Implement profile picture update logic

package com.youbid.fyp.service;

import com.youbid.fyp.config.JwtProvider;
import com.youbid.fyp.model.User;
import com.youbid.fyp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class UserServiceImplementation implements UserService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    FileStorageService fileStorageService;

    @Autowired
    private LoyaltyService loyaltyService;

    //we are not using this register function , this was for testing only
    @Override
    public User registerUser(User user) {

        User newUser = new User();
        newUser.setEmail(user.getEmail());
        newUser.setPassword(user.getPassword());
        newUser.setFirstname(user.getFirstname());
        newUser.setLastname(user.getLastname());
        newUser.setRole(user.getRole());
        newUser.setBalance(user.getBalance());
        newUser.setGender(user.getGender());

        User savedUser = userRepository.save(newUser);
        return savedUser;
    }

    @Override
    public User findUserById(Integer userId) throws Exception {

        Optional<User> user = userRepository.findById(userId);

        if(user.isPresent()){
            return user.get();
        }
        throw new Exception("User does not exist with id: " + userId);
    }

    @Override
    public User findUserByEmail(String email) {
        User user = userRepository.findByEmail(email);
        return user;
    }

    @Override
    public User updateUser(User user, Integer userId) throws Exception {
        Optional<User> user1 = userRepository.findById(userId);

        if(user1.isEmpty()){
            throw new Exception("User does not exist with id: " + userId);
        }

        User oldUser = user1.get();

        if(user.getFirstname() != null){
            oldUser.setFirstname(user.getFirstname());
        }

        if(user.getLastname() != null){
            oldUser.setLastname(user.getLastname());
        }

        if(user.getEmail() != null){
            oldUser.setEmail(user.getEmail());
        }
        if(user.getGender() != null){
            oldUser.setGender(user.getGender());
        }
        if(user.getBalance() != null){
            oldUser.setBalance(user.getBalance());
        }

        if(user.getCellphone() != null){
            oldUser.setCellphone(user.getCellphone());
        }

        if(user.getBanned() != null){
            oldUser.setBanned(user.getBanned());
        }

        if(user.getSuspended() != null){
            oldUser.setSuspended(user.getSuspended());
        }

        if(user.getSuspensionDate() != null){
            if(user.getSuspended() == false)
            {
                oldUser.setSuspensionDate(null);
            }
            else {
                oldUser.setSuspensionDate(user.getSuspensionDate());
            }
        }

        if(user.getProfilePicture() != null) {
            oldUser.setProfilePicture(user.getProfilePicture());
        }

        User updatedUser = userRepository.save(oldUser);

        return updatedUser;
    }

    @Override
    public List<User> searchUser(String query) {
        return userRepository.searchUser(query);
    }

    @Override
    public User findUserByJwt(String jwt){
        String email = JwtProvider.getEmailFromJwtToken(jwt);
        User user = userRepository.findByEmail(email);
        return user;
    }

    @Override
    public User updateProfilePicture(Integer userId, MultipartFile file) throws Exception {
        Optional<User> userOpt = userRepository.findById(userId);

        if(userOpt.isEmpty()) {
            throw new Exception("User does not exist with id: " + userId);
        }

        User user = userOpt.get();

        // Delete old profile picture if exists
        if(user.getProfilePicture() != null && !user.getProfilePicture().isEmpty()) {
            fileStorageService.deleteProfilePicture(user.getProfilePicture());
        }

        // Store new profile picture
        String profilePictureName = fileStorageService.storeProfilePicture(file);
        user.setProfilePicture(profilePictureName);

        return userRepository.save(user);
    }

    /**
     * Get the loyalty status for a user - convenience method
     */
    @Override
    public Map<String, Object> getLoyaltyStatus(Integer userId) throws Exception {
        return loyaltyService.getUserLoyaltyStatus(userId);
    }
}
package com.youbid.fyp.service;


import com.youbid.fyp.config.JwtProvider;
import com.youbid.fyp.model.User;

import com.youbid.fyp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImplementation implements UserService {

    @Autowired
    UserRepository userRepository;

    @Override
    public User registerUser(User user) {

        User newUser = new User();
        newUser.setEmail(user.getEmail());
        newUser.setPassword(user.getPassword());
        newUser.setFirstname(user.getFirstname());
        newUser.setLastname(user.getLastname());
        newUser.setRole(user.getRole()); //every user will be user, we will make seperate controller for admins later on
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

}
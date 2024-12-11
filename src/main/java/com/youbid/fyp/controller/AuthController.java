package com.youbid.fyp.controller;


import com.youbid.fyp.config.JwtProvider;
import com.youbid.fyp.model.User;
import com.youbid.fyp.repository.UserRepository;
import com.youbid.fyp.request.LoginRequest;
import com.youbid.fyp.response.AuthResponse;
import com.youbid.fyp.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CustomUserDetailsService customUserDetails;

    @PostMapping("/register")
    public AuthResponse createUser(@RequestBody User user) throws Exception {
        User isExist = userRepository.findByEmail(user.getEmail());

        if (isExist != null) {
            throw new Exception("Email already in use by another account!");
        }

        User newUser = new User();

        newUser.setEmail(user.getEmail());
        newUser.setPassword(passwordEncoder.encode(user.getPassword()));
        newUser.setFirstname(user.getFirstname());
        newUser.setLastname(user.getLastname());
        newUser.setRole(user.getRole());
        newUser.setGender(user.getGender());
        newUser.setBalance(user.getBalance());

        if(newUser.getRole().equals("")){
            newUser.setRole("USER");
        }
        User savedUser = userRepository.save(newUser);
        Authentication authentication = new UsernamePasswordAuthenticationToken(user.getEmail(), user.getPassword());

        String token = JwtProvider.generateToken(authentication, savedUser);
        AuthResponse res = new AuthResponse(token, "Registered Successfully! :)", savedUser);

        return res;
    }

    @PostMapping("/login")
    public AuthResponse signin(@RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticate(loginRequest.getEmail(), loginRequest.getPassword());

        User user = userRepository.findByEmail(loginRequest.getEmail());
        String token = JwtProvider.generateToken(authentication, user);

        AuthResponse res = new AuthResponse(token, "Login Successful!",user);
        return res;
    }


    private Authentication authenticate(String email, String password){
        UserDetails userDetails = customUserDetails.loadUserByUsername(email);


        if(userDetails == null){
            throw new BadCredentialsException("Invalid username!");
        }

        if(!passwordEncoder.matches(password, userDetails.getPassword())){
            throw new BadCredentialsException("Password not matched!");
        }
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

}

package com.youbid.fyp.controller;



import com.youbid.fyp.config.JwtProvider;
import com.youbid.fyp.model.User;
import com.youbid.fyp.model.Product;
import com.youbid.fyp.repository.UserRepository;
import com.youbid.fyp.response.AuthResponse;
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

import java.util.List;


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

}

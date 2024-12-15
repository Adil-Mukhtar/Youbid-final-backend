package com.youbid.fyp.service;


import com.youbid.fyp.model.Product;
import com.youbid.fyp.model.User;
import com.youbid.fyp.repository.ProductRepository;
import com.youbid.fyp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ProductServiceImplementation implements ProductService {

    @Autowired
    ProductRepository productRepository;

    @Autowired
    UserService userService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    CategoryService categoryService;

    /*
    @Override
    public Product createProduct(Product product, Integer userId) throws Exception {

        User user = userService.findUserById(userId);
        Product newProduct = new Product();
        newProduct.setName(product.getName());
        newProduct.setPrice(product.getPrice());
        newProduct.setCategory(product.getCategory());
        newProduct.setDescription(product.getDescription());
        newProduct.setStatus(product.getStatus());
        newProduct.setCreatedAt(LocalDateTime.now()); //current time from system
        newProduct.setUser(user);

        return productRepository.save(newProduct);
    }
     */

    @Override
    public Product createProduct(Product product, Integer userId) throws Exception {

        User user = userService.findUserById(userId);
        Product newProduct = new Product();
        newProduct.setName(product.getName());
        newProduct.setPrice(product.getPrice());
        newProduct.setDescription(product.getDescription());
        newProduct.setStatus("pending");
        newProduct.setUser(user);
        newProduct.setCreatedAt(LocalDateTime.now()); // Current time from system
        newProduct.setLocation(product.getLocation());// Set the location
        newProduct.setCategory(product.getCategory());

        return productRepository.save(newProduct);
    }

    @Override
    public String deleteProduct(Integer id, Integer userId) throws Exception {

        Product product = findProductById(id);
        User user = userService.findUserById(userId);
        if(product.getUser().getId() != user.getId()){
            throw new Exception("You can't delete someone else's product");
        }
        productRepository.delete(product);
        return "Product deleted successfully!";
    }

    @Override
    public Product updateProduct(Product product, Integer userId, Integer productId) throws Exception {

        Product oldProduct = findProductById(productId);
        if(oldProduct.getId() == null) {
            throw new Exception("Product not found with id: " + product.getId());
        }

        User user = userService.findUserById(userId);



        if(product.getName() != null){
            oldProduct.setName(product.getName());
        }
        if(product.getPrice() != null){
            oldProduct.setPrice(product.getPrice());
        }
        if(product.getDescription() != null){
            oldProduct.setDescription(product.getDescription());
        }
        if(product.getLocation() != null){
            oldProduct.setLocation(product.getLocation());
        }
        if(product.getCategory() != null){
            oldProduct.setCategory(product.getCategory());
        }

        Product updatedProduct = productRepository.save(oldProduct);

        return updatedProduct;
    }

    @Override
    public List<Product> findProductsByUserId(Integer userId) {
        System.out.println(userId);
        return productRepository.findProductByUserId(userId);
    }

    @Override
    public Product findProductById(Integer productId) throws Exception {

        Optional<Product> opt = productRepository.findById(productId);

        if(opt.isEmpty()){
            throw new Exception("Product not found with id: " + productId);
        }
        return opt.get();
    }

    @Override
    public List<Product> findAllProducts() {

        return productRepository.findAll();
    }


    //for admin

    @Override
    public Product updateProductbyAdmin(Product product, Integer productId) throws Exception {

        Product oldProduct = findProductById(productId);
        if(oldProduct.getId() == null) {
            throw new Exception("Product not found with id: " + product.getId());
        }

        if(product.getName() != null){
            oldProduct.setName(product.getName());
        }
        if(product.getPrice() != null){
            oldProduct.setPrice(product.getPrice());
        }
        if(product.getDescription() != null){
            oldProduct.setDescription(product.getDescription());
        }
        if(product.getStatus() != null){
            oldProduct.setStatus(product.getStatus());
        }
        if(product.getLocation() != null){
            oldProduct.setLocation(product.getLocation());
        }
        if(product.getCategory() != null){
            oldProduct.setCategory(product.getCategory());
        }

        Product updatedProduct = productRepository.save(oldProduct);

        return updatedProduct;
    }

    //for admin
    @Override
    public String deleteProductByAdmin(Integer productId) throws Exception {

        Product product = findProductById(productId);
        productRepository.delete(product);
        return "Product deleted successfully!";
    }

    //for public to search products

    @Override
    public List<Product> searchProducts(String query, String location, String category) {
        // Case 1: All filters are null
        if (query == null && location == null && category == null) {
            return productRepository.findAll();
        }

        // Case 2: Apply filtering conditions
        return productRepository.searchProducts(query, location, category);
    }
    //hello
}

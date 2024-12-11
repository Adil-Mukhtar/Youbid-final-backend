package com.youbid.fyp.service;

import com.youbid.fyp.model.Product;

import java.util.List;

public interface ProductService {
    Product createProduct(Product product, Integer userId) throws Exception;

    String deleteProduct(Integer id, Integer userId) throws Exception;

    List<Product> findProductsByUserId(Integer userId);

    Product findProductById(Integer productId) throws Exception;

    List<Product> findAllProducts();

    Product updateProduct(Product product, Integer userId, Integer productId) throws Exception;
}

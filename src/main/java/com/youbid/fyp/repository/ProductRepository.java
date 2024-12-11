package com.youbid.fyp.repository;

import com.youbid.fyp.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Integer> {

    @Query("SELECT p FROM Product p WHERE p.user.id = :userId")
    List<Product> findProductByUserId (Integer userId);
}

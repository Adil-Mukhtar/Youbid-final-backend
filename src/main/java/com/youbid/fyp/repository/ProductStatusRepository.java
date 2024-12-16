package com.youbid.fyp.repository;

import com.youbid.fyp.model.ProductStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductStatusRepository extends JpaRepository<ProductStatus, Integer> {
}

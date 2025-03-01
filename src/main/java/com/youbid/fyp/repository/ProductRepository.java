package com.youbid.fyp.repository;

import com.youbid.fyp.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Integer> {

    @Query("SELECT p FROM Product p WHERE p.user.id = :userId")
    List<Product> findProductByUserId (Integer userId);


//    @Query("SELECT p FROM Product p WHERE " +
//            "(:query IS NULL OR p.name ILIKE CONCAT('%', :query, '%')) AND " +
//            "(:location IS NULL OR p.location ILIKE :location) AND " +
//            "(:category IS NULL OR p.category ILIKE :category)")
//    List<Product> searchProducts(@Param("query") String query,
//                                 @Param("location") String location,
//                                 @Param("category") String category);

//    @Query("SELECT p FROM Product p WHERE " +
//            "(COALESCE(:query, '') = '' OR LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%'))) " +
//            "AND (COALESCE(:location, '') = '' OR LOWER(p.location) LIKE LOWER(CONCAT('%', :location, '%'))) " +
//            "AND (COALESCE(:category, '') = '' OR LOWER(p.category) LIKE LOWER(CONCAT('%', :category, '%')))")
//    List<Product> searchProducts(@Param("query") String query,
//                                 @Param("location") String location,
//                                 @Param("category") String category);

    @Query("SELECT p FROM Product p WHERE " +
            "(COALESCE(:query, '') = '' OR LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%'))) " +
            "AND (COALESCE(:location, '') = '' OR LOWER(p.location) LIKE LOWER(CONCAT('%', :location, '%'))) " +
            "AND (COALESCE(:category, '') = '' OR LOWER(p.category) LIKE LOWER(CONCAT('%', :category, '%'))) " +
            "AND (:minPrice IS NULL OR p.price >= :minPrice) " +
            "AND (:maxPrice IS NULL OR p.price <= :maxPrice)")
    List<Product> searchProducts(
            @Param("query") String query,
            @Param("location") String location,
            @Param("category") String category,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice
    );



    //AFTER bidding won
    @Query("SELECT p FROM Product p WHERE p.auctionDeadline < CURRENT_TIMESTAMP AND p.status = 'live'")
    List<Product> findExpiredAuctions();

}

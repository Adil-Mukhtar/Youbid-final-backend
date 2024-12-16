package com.youbid.fyp.service;

import com.youbid.fyp.model.ProductStatus;

public interface ProductStatusService {

    ProductStatus createProductStatus () throws Exception;
    ProductStatus updateProductStatus(String status) throws Exception;
    ProductStatus getProductStatusById(int id) throws Exception;

}

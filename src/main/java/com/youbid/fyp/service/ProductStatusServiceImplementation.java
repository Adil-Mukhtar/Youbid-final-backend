package com.youbid.fyp.service;

import com.youbid.fyp.model.Product;
import com.youbid.fyp.model.ProductStatus;
import com.youbid.fyp.repository.ProductStatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ProductStatusServiceImplementation implements ProductStatusService {

    @Autowired
    private ProductStatusRepository productStatusRepository;
    @Override
    public ProductStatus createProductStatus() throws Exception {
        ProductStatus productStatus = new ProductStatus();
        productStatus.setStatus("pending");
        productStatus.setId(1);
        return productStatusRepository.save(productStatus);
    }

    @Override
    public ProductStatus updateProductStatus(String status) throws Exception {
        ProductStatus productStatus = productStatusRepository.findById(1).get();;
        productStatus.setStatus(status);
        return productStatusRepository.save(productStatus);
    }

    @Override
    public ProductStatus getProductStatusById(int statusId) throws Exception {
        Optional<ProductStatus> opt = productStatusRepository.findById(statusId);

        if(opt.isEmpty()){
            throw new Exception("Product not found with id: " + statusId);
        }
        return opt.get();
    }

}

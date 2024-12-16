package com.youbid.fyp.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class ProductStatus {

    @Id
    private Integer id;
    private String status;

    public ProductStatus() {

    }

    public ProductStatus(Integer id, String status) {
        this.id = id;
        this.status = status;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

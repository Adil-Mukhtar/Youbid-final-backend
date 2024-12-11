package com.youbid.fyp.controller;

import com.youbid.fyp.model.Category;
import com.youbid.fyp.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("category")
public class CategoryController {

    @Autowired
    CategoryService categoryService;

    @PostMapping("create")
    public ResponseEntity<Category> createCategory(@RequestBody Category category) throws Exception {

        Category newCategory = categoryService.createCategory(category);

        return new ResponseEntity<>(newCategory, HttpStatus.CREATED);
    }

}

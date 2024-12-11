package com.youbid.fyp.service;

import com.youbid.fyp.model.Category;
import com.youbid.fyp.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CategoryServiceImplementation implements CategoryService {

    @Autowired
    CategoryRepository categoryRepository;

    @Override
    public Category createCategory(Category category) throws Exception {

        Category category1 = new Category();
        category1.setName(category.getName());
        return categoryRepository.save(category1);
    }

    @Override
    public Category findCategoryById(Integer catId) throws Exception {
        Optional<Category> opt = categoryRepository.findById(catId);
        if(opt.isEmpty()){
            throw new Exception("Category not found with id: " + catId);
        }
        return opt.get();
    }
}

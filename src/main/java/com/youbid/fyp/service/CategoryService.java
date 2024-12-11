package com.youbid.fyp.service;

import com.youbid.fyp.model.Category;

public interface CategoryService {
    Category createCategory(Category category) throws Exception;

    Category findCategoryById(Integer catId) throws Exception;
}

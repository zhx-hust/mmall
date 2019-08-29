package com.mmall.service;

import com.mmall.common.ServerResponse;
import com.mmall.pojo.Category;

import java.util.List;

public interface ICategoryService {
    ServerResponse addCategory(String categoryName, Integer parentId);

    ServerResponse updateCategory(String categoryName, Integer categoryId);

    ServerResponse<List<Category>> getFirstGenerationChildren(Integer categoryId);

    ServerResponse<List<Integer>> getAllGenerationsChildren(Integer categoryId);
}

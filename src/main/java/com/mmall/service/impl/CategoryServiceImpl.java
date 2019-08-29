package com.mmall.service.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mmall.common.ServerResponse;
import com.mmall.dao.CategoryMapper;
import com.mmall.pojo.Category;
import com.mmall.service.ICategoryService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.PublicKey;
import java.util.List;
import java.util.Set;

@Service("iCategoryService")
public class CategoryServiceImpl implements ICategoryService {
    private Logger logger = LoggerFactory.getLogger(CategoryServiceImpl.class);

    @Autowired
    private CategoryMapper categoryMapper;

    public ServerResponse addCategory(String categoryName,Integer parentId){
        if((parentId==null)||StringUtils.isBlank(categoryName)){
            return ServerResponse.createByErrorMessage("添加商品参数");
        }
        Category category=new Category();
        category.setName(categoryName);
        category.setParentId(parentId);
        category.setStatus(true);

        int rowCount =categoryMapper.insert(category);
        if(rowCount>0){
            return ServerResponse.createBySuccess("添加品类成功");
        }
        return ServerResponse.createByErrorMessage("添加品类失败");
    }

    public ServerResponse updateCategory(String categoryName, Integer categoryId){
        if((categoryId==null)||(StringUtils.isBlank(categoryName))){
            return ServerResponse.createByErrorMessage("更新品类参数错误");
        }
        Category category= new Category();
        category.setId(categoryId);
        category.setName(categoryName);

        int rowCount =categoryMapper.updateByPrimaryKeySelective(category);
        if(rowCount>0){
            return ServerResponse.createBySuccessMessage("更新品类名字成功");
        }
        return  ServerResponse.createByErrorMessage("更新品类名字失败");
    }

    public ServerResponse<List<Category>> getFirstGenerationChildren(Integer categoryId){
        List<Category> categoryList=categoryMapper.selectChildrenByCategoryId(categoryId);
        if(CollectionUtils.isEmpty(categoryList)){
            logger.info("未找到当前分类的子分类");
        }
        return ServerResponse.createBySuccess(categoryList);
    }

    @Override
    public ServerResponse<List<Integer>> getAllGenerationsChildren(Integer categoryId) {
        Set<Category> categorySet= Sets.newHashSet();
        getAllGenerations(categorySet,categoryId);

        List<Integer> categoryIdList = Lists.newArrayList();
        if(categoryId!=null){
            for(Category category:categorySet){
                categoryIdList.add(category.getId());
            }
        }

        return ServerResponse.createBySuccess(categoryIdList);
    }

    private Set<Category> getAllGenerations(Set<Category> categorySet, Integer categoryId) {
        Category category=categoryMapper.selectByPrimaryKey(categoryId);
        if(category!=null){
            categorySet.add(category);
        }
        List<Category> categoryList=categoryMapper.selectChildrenByCategoryId(categoryId);
        for (Category category1:
             categoryList) {
            getAllGenerations(categorySet,category1.getId());
        }
        return categorySet;
    }



}

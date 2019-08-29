package com.mmall.dao;

import com.mmall.pojo.Cart;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CartMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Cart record);

    int insertSelective(Cart record);

    Cart selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Cart record);

    int updateByPrimaryKey(Cart record);

    Cart selectByUserIdProductId(@Param("userId") Integer userId,@Param("productId") Integer productId);

    List<Cart> selectByUserId(Integer userId);

    int selectUncheckedByUserId (Integer userId);

    int deleteByUserIdproductIds(@Param("userId") Integer userId,@Param("productId") List<String> productIdList);

    int chekcedorUnchecked(@Param("userId")Integer userId, @Param("productId") Integer productId,@Param("productId")Integer checked);

    int getTotolNum(@Param("userId") Integer userId);

    List<Cart> selectCheckedCartByUserId(Integer userId);

}
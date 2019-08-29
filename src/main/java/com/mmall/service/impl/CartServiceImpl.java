package com.mmall.service.impl;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.dao.CartMapper;
import com.mmall.dao.ProductMapper;
import com.mmall.pojo.Cart;
import com.mmall.pojo.Product;
import com.mmall.service.ICartService;
import com.mmall.util.BigDecimalUtil;
import com.mmall.util.PropertiesUtil;
import com.mmall.vo.CartProductVo;
import com.mmall.vo.CartVo;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service("iCartService")
public class CartServiceImpl implements ICartService {
    @Autowired
    private CartMapper cartMapper;
    @Autowired
    private ProductMapper productMapper;


    @Override
    public ServerResponse<CartVo> list(Integer userId) {
        CartVo cartVo = this.getCartVoLimit(userId);
        return ServerResponse.createBySuccess(cartVo);
    }

    public ServerResponse lsit(Integer userId) {
        CartVo cartVo = this.getCartVoLimit(userId);
        return ServerResponse.createBySuccess(cartVo);
    }


    @Override
    public ServerResponse add(Integer userId, Integer productId, Integer count) {
        if ((productId == null) && count == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Cart cart = cartMapper.selectByUserIdProductId(userId, productId);
        if (cart == null) {
            //购物车不存在，新增insert
            Cart cartNew = new Cart();
            cartNew.setQuantity(count);
            cartNew.setChecked(Const.Cart.CHECKED);
            cartNew.setProductId(productId);
            cartNew.setUserId(userId);
            cartMapper.insert(cartNew);
        } else {
            cart.setQuantity(cart.getQuantity() + count);
            cartMapper.updateByPrimaryKeySelective(cart);
        }
        //一般要回到购物车页面
        CartVo cartVo = this.getCartVoLimit(userId);
        return ServerResponse.createBySuccess(cartVo);
    }

    @Override
    public ServerResponse update(Integer userId, Integer productId, Integer count) {
        if (productId == null && count == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Cart cart = cartMapper.selectByUserIdProductId(userId, productId);
        if (cart != null) {
            cart.setQuantity(count);
        }
        cartMapper.updateByPrimaryKeySelective(cart);
        CartVo cartVo = this.getCartVoLimit(userId);
        return ServerResponse.createBySuccess(cartVo);
    }

    @Override
    public ServerResponse del(Integer userId, String productIds) {
        List<String> productIdList = Splitter.on(",").splitToList(productIds);
        if (CollectionUtils.isEmpty(productIdList)) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        cartMapper.deleteByUserIdproductIds(userId, productIdList);
        CartVo cartVo = this.getCartVoLimit(userId);
        return ServerResponse.createBySuccess(cartVo);
    }

    //    @Override
//    public ServerResponse selectAll(Integer userId) {
//         cartMapper.chekcedAll(userId);
//        CartVo cartVo = this.getCartVoLimit(userId);
//        return ServerResponse.createBySuccess(cartVo);
//    }
//    @Override
//    public ServerResponse unselectAll(Integer userId) {
//        cartMapper.unchekcedAll(userId);
//        CartVo cartVo = this.getCartVoLimit(userId);
//        return ServerResponse.createBySuccess(cartVo);
//    }两个方法合并
    @Override

//    public ServerResponse selectOrUnSelectAll(Integer userId, Integer checked) {
//        cartMapper.chekcedorUnchecked(userId, checked);
//        CartVo cartVo = this.getCartVoLimit(userId);
//        return ServerResponse.createBySuccess(cartVo);
//    }
//
//    @Override
//    public ServerResponse selectOrUnSelect(Integer userId, Integer productId, int checked) {
//        cartMapper.chekcedorUnchecked(userId,productId, checked);
//        CartVo cartVo = this.getCartVoLimit(userId);
//        return ServerResponse.createBySuccess(cartVo);
//    }
    //以上两个也可以合并成一个实现
    public ServerResponse selectOrUnSelect(Integer userId, Integer productId, int checked) {
        cartMapper.chekcedorUnchecked(userId,productId, checked);
        CartVo cartVo = this.getCartVoLimit(userId);
        return ServerResponse.createBySuccess(cartVo);
    }

    @Override
    public ServerResponse<Integer> getNum(Integer userId) {
        if(userId == null){
            return ServerResponse.createBySuccess(0);
        }
        return ServerResponse.createBySuccess(cartMapper.getTotolNum(userId));
    }
//这种查数量的方法有问题吗

    public ServerResponse<Integer> getNum1(Integer userId) {
        List<Cart> cartList =cartMapper.selectByUserId(userId);
        Integer totalNum = 0;
        if(cartList!=null){
            for (Cart cartItem :
                    cartList) {
                totalNum += cartItem.getQuantity();
            }
        }
        return ServerResponse.createBySuccess(totalNum);
    }



    private CartVo getCartVoLimit(Integer userId) {
        List<Cart> cartList = cartMapper.selectByUserId(userId);
        List<CartProductVo> cartProductVoList = Lists.newArrayList();
        BigDecimal cartTotal = new BigDecimal("0");
        if (CollectionUtils.isNotEmpty(cartList)) {
            for (Cart cartItem : cartList) {
                CartProductVo cartProductVo = new CartProductVo();
                cartProductVo.setId(cartItem.getId());
                cartProductVo.setUserId(cartItem.getUserId());
                cartProductVo.setProductId(cartItem.getProductId());

                Product product = productMapper.selectByPrimaryKey(cartItem.getProductId());
                if (product != null) {
                    cartProductVo.setProductName(product.getName());
                    cartProductVo.setProductSubtitle(product.getSubtitle());
                    cartProductVo.setProductMainImage(product.getMainImage());
                    cartProductVo.setProductStatus(product.getStatus());
                    cartProductVo.setProductPrice(product.getPrice());
                    cartProductVo.setProductStock(product.getStock());

                    int limitQuantity = 0;
                    if (cartItem.getQuantity() <= product.getStock()) {
                        //
                        limitQuantity = cartItem.getQuantity();
                        cartProductVo.setLimitQuantity(Const.Cart.LIMIT_NUM_SUCCESS);
                    } else {
                        limitQuantity = product.getStock();
                        cartProductVo.setLimitQuantity(Const.Cart.LIMIT_NUM_FAIL);
                        //更新cart中的数量。特殊情况是，一开是加入购物车，但未支付。别人购买了库存下降，使得这个购物车中的quantity大于stock，所以要更新quantity
                        Cart cart = new Cart();
                        cart.setId(cart.getId());
                        cart.setQuantity(product.getStock());
                        cartMapper.updateByPrimaryKey(cart);
                    }
                    cartProductVo.setQuantity(limitQuantity);
                    cartProductVo.setProductTotalPrice(BigDecimalUtil.mul(product.getPrice().doubleValue(), cartProductVo.getQuantity()));
                    cartProductVo.setProductChecked(cartItem.getChecked());
                }
                if (cartProductVo.getProductChecked() == Const.Cart.CHECKED) {
                    cartTotal = BigDecimalUtil.add(cartTotal.doubleValue(), cartProductVo.getProductTotalPrice().doubleValue());
                }
                cartProductVoList.add(cartProductVo);
            }
        }
        CartVo cartVo = new CartVo();
        cartVo.setCartProductVoList(cartProductVoList);
        cartVo.setTotalPrice(cartTotal);
        cartVo.getAllchekced(this.getAllChecked(userId));
        cartVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));


        return cartVo;

    }

    private boolean getAllChecked(Integer userId) {
        if (userId == null) {
            return false;
        }
        return cartMapper.selectUncheckedByUserId(userId) == 0 ? true : false;
    }
}


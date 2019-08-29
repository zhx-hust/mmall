package com.mmall.vo;

import com.mmall.pojo.Cart;

import java.math.BigDecimal;
import java.util.List;

public class CartVo {
    private List<CartProductVo> cartProductVoList;
    private String imageHost;
    private BigDecimal totalPrice;
    private Boolean allchekced;

    public List<CartProductVo> getCartProductVoList() {
        return cartProductVoList;
    }

    public void setCartProductVoList(List<CartProductVo> cartProductVoList) {
        this.cartProductVoList = cartProductVoList;
    }

    public String getImageHost() {
        return imageHost;
    }

    public void setImageHost(String imageHost) {
        this.imageHost = imageHost;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public Boolean getAllchekced(boolean allChecked) {
        return allchekced;
    }

    public void setAllchekced(Boolean allchekced) {
        this.allchekced = allchekced;
    }
}

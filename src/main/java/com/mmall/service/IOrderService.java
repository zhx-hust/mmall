package com.mmall.service;

import com.github.pagehelper.PageInfo;
import com.mmall.common.ServerResponse;
import com.mmall.vo.OrderVo;

public interface IOrderService {
    ServerResponse create(Integer userId, Integer shippingId);

    ServerResponse cancel(Integer userId, Long orderId);

    ServerResponse getOrderCartProduct(Integer userId);

    ServerResponse detail(Integer userId, Long orderNo);


    ServerResponse<PageInfo> list(Integer userId, Integer pageNum, Integer pageSize);

    ServerResponse<PageInfo> manageList(int pageNum, int pageSize);

    ServerResponse<OrderVo> orderdetail(Long orderNo);


    ServerResponse<PageInfo> search(Long orderNo, int pageNum, int pageSize);
}

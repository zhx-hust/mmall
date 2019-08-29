package com.mmall.service;

import com.mmall.common.ServerResponse;
import com.mmall.vo.CartVo;

public interface ICartService {
    ServerResponse<CartVo> list(Integer userId);

    ServerResponse add(Integer userId, Integer productId, Integer count);

    ServerResponse update(Integer userId, Integer productId, Integer count);

    ServerResponse del(Integer userId, String productIds);

    ServerResponse lsit(Integer userId);

    ServerResponse selectOrUnSelect(Integer userId, Integer productId, int checked);

    ServerResponse<Integer> getNum(Integer userId);
}

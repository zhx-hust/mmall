package com.mmall.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mmall.common.ServerResponse;
import com.mmall.controller.portal.ShippingController;
import com.mmall.dao.ShippingMapper;
import com.mmall.pojo.Shipping;
import com.mmall.service.IShippingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;


@Service("IShippingService")
public class ShippingServiceImpl implements IShippingService {
    @Autowired
    private ShippingMapper shippingMapper;

    @Override
    public ServerResponse add(Integer id, Shipping shipping) {
        shipping.setUserId(id);
        int rowCount = shippingMapper.insert(shipping);
        if (rowCount > 0) {
            Map result = Maps.newHashMap();
            result.put("shippingId", shipping.getId());
            return ServerResponse.createBySuccess("新建地址成功", result);
        }
        return ServerResponse.createByErrorMessage("新建地址失败");
    }

    @Override
    public ServerResponse del(Integer userId, Integer shippingId) {
        int resultCount = shippingMapper.deleteByShippingIdUserId(shippingId, userId);
        if (resultCount > 0) {
            return ServerResponse.createBySuccess("删除地址成功");
        }
        return ServerResponse.createBySuccess("删除地址失败");
    }

    @Override
    public ServerResponse update(Integer userId, Shipping shipping) {
        shipping.setUserId(userId);
        int rowCount = shippingMapper.updateByShipping(shipping);
        if (rowCount > 0) {
            return ServerResponse.createBySuccess("修改地址成功");
        }
        return ServerResponse.createByErrorMessage("修改地址失败");


    }

    @Override
    public ServerResponse select(Integer userId, Integer shippingId) {
        Shipping shipping =shippingMapper.selectByShippingIdUserId(userId,shippingId);
        if(shipping!=null){
            return ServerResponse.createBySuccess(shipping);

//         源码的更新地址成功？？   return ServerResponse.createBySuccess("更新地址成功",shipping);
        }
        return ServerResponse.createByErrorMessage("无法查询到该用户地址");
    }

    @Override
    public ServerResponse<PageInfo> list(Integer userId, Integer pageNum, Integer pageSize) {

        List<Shipping>  shippingList=shippingMapper.selectByUserId(userId);
        PageHelper.startPage(pageNum,pageSize);
        PageInfo pageInfo =new PageInfo(shippingList);
        return ServerResponse.createBySuccess(pageInfo);
    }


}

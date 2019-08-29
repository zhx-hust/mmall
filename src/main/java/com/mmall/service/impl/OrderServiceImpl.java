package com.mmall.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.dao.*;
import com.mmall.pojo.*;
import com.mmall.service.IOrderService;
import com.mmall.service.IShippingService;
import com.mmall.util.BigDecimalUtil;
import com.mmall.util.DateTimeUtil;
import com.mmall.util.PropertiesUtil;
import com.mmall.vo.OrderItemVo;
import com.mmall.vo.OrderProductVo;
import com.mmall.vo.OrderVo;
import com.mmall.vo.ShippingVo;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Random;

@Service("iOrderService")
public class OrderServiceImpl implements IOrderService {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private CartMapper cartMapper;
    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private OrderItemMapper orderItemMapper;
    @Autowired
    private ShippingMapper shippingMapper;

    public ServerResponse create(Integer userId, Integer shippingId) {
        List<Cart> cartList = cartMapper.selectByUserId(userId);
        ServerResponse serverResponse = this.formOrderItem(userId, cartList);
        if (!serverResponse.isSuccess()) {
            return serverResponse;
        }
        List<OrderItem> orderItemList = (List<OrderItem>) serverResponse.getData();
        BigDecimal payment = getOrderTotalPrice(orderItemList);
        Order order = this.formOrder(userId, shippingId, payment);
        if (order == null) {
            return ServerResponse.createByErrorMessage("生成订单错误");
        }

        orderItemMapper.batchInsert(orderItemList);
        this.redueProductStock(orderItemList);
        this.clearCart(cartList);

        OrderVo orderVo = formOrderVo(order, orderItemList);
        return ServerResponse.createBySuccess(orderVo);


    }

    @Override
    public ServerResponse cancel(Integer userId, Long orderId) {
        Order order = (Order) orderMapper.selectByUserIdOrderNo(userId, orderId);
        if (order == null) {
            return ServerResponse.createByErrorMessage("该用户此订单不存在");
        }
        if (order.getStatus() != Const.OrderStatusEnum.NO_PAY.getCode()) {
            return ServerResponse.createByErrorMessage("已付款,无法取消订单");
        }
        Order orderNew = new Order();
        orderNew.setId(order.getId());
        order.setStatus(Const.OrderStatusEnum.CANCELED.getCode());

        int row = orderMapper.updateByPrimaryKeySelective(orderNew);
        if (row > 0) {
            return ServerResponse.createBySuccess();
        }
        return ServerResponse.createByError();


    }

    @Override
    public ServerResponse getOrderCartProduct(Integer userId) {
        OrderProductVo orderProductVo = new OrderProductVo();
        List<Cart> cartList = cartMapper.selectCheckedCartByUserId(userId);
        ServerResponse serverResponse = this.formOrderItem(userId, cartList);
        if (!serverResponse.isSuccess()) {
            return serverResponse;
        }
        List<OrderItem> orderItemList = (List<OrderItem>) serverResponse.getData();
        List<OrderItemVo> orderItemVoList = Lists.newArrayList();
        BigDecimal payment = new BigDecimal("0");
        for (OrderItem orderItem : orderItemList) {
            OrderItemVo orderItemVo = new OrderItemVo();
            orderItemVo = formOrderItemVo(orderItem);
            orderItemVoList.add(orderItemVo);
            payment = BigDecimalUtil.add(payment.doubleValue(), orderItem.getTotalPrice().doubleValue());
        }
        orderProductVo.setProductTotalPrice(payment);
        orderProductVo.setOrderItemVoList(orderItemVoList);
        orderProductVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));
        return ServerResponse.createBySuccess(orderProductVo);
    }

    @Override
    public ServerResponse detail(Integer userId, Long orderNo) {
        Order order = orderMapper.selectByUserIdOrderNo(userId, orderNo);
        if (order == null) {
            return ServerResponse.createByErrorMessage("没有找到该订单");
        }
        List<OrderItem> orderItemList = orderItemMapper.selectByUserIdOrderNo(userId, orderNo);
        OrderVo orderVo = formOrderVo(order, orderItemList);
        return ServerResponse.createBySuccess(orderVo);


    }

    @Override
    public ServerResponse<PageInfo> list(Integer userId, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Order> orderList = orderMapper.selectByUserId(userId);
        List<OrderVo> orderVoList = formOrderVoList(orderList, userId);
        PageInfo pageInfo = new PageInfo(orderList);
        pageInfo.setList(orderVoList);
        return ServerResponse.createBySuccess(pageInfo);
    }


    private List<OrderVo> formOrderVoList(List<Order> orderList, Integer userId) {
        List<OrderVo> orderVoList = Lists.newArrayList();
        for (Order order : orderList) {
//这三行适用用户查询
//              List<OrderItem> orderItemList = orderItemMapper.selectByUserIdOrderNo(userId, order.getOrderNo());
//            OrderVo orderVo = formOrderVo(order, orderItemList);
//            orderVoList.add(orderVo);
            List<OrderItem> orderItemList = Lists.newArrayList();
            if (userId == null) {
                orderItemList = orderItemMapper.selectByOrderNo(order.getOrderNo());
            } else {
                orderItemList = orderItemMapper.selectByUserIdOrderNo(userId, order.getOrderNo());
            }
            OrderVo orderVo = formOrderVo(order, orderItemList);
            orderVoList.add(orderVo);
        }
        return orderVoList;

    }


    private OrderVo formOrderVo(Order order, List<OrderItem> orderItemList) {
        OrderVo orderVo = new OrderVo();
        //        private Long orderNo;
        orderVo.setOrderNo(order.getOrderNo());
//        private BigDecimal payment;
        orderVo.setPayment(order.getPayment());
//        private Integer paymentType;
        orderVo.setPaymentType(order.getPaymentType());
//        private String paymentTypeDesc;
        orderVo.setPaymentTypeDesc(Const.PaymentTypeEnum.codeOf(order.getPaymentType()).getValue());
//        private Integer postage;
        orderVo.setPostage(order.getPostage());
//        private Integer status;
        orderVo.setStatus(order.getStatus());
//        private String statusDesc;
        orderVo.setStatusDesc(Const.OrderStatusEnum.codeOf(order.getStatus()).getValue());
        //        private Integer shippingId;
//        private String receiverName;
//        private ShippingVo shippingVo;
        orderVo.setShippingId(order.getShippingId());
        Shipping shipping = shippingMapper.selectByPrimaryKey(order.getShippingId());
        if (shipping != null) {
            orderVo.setReceiverName(shipping.getReceiverName());
            orderVo.setShippingVo(formShippingVO(shipping));
        }
//        //订单的明细
//        private List<OrderItemVo> orderItemVoList;
        List<OrderItemVo> orderItemVoList = Lists.newArrayList();
        for (OrderItem orderItem : orderItemList) {
            OrderItemVo orderItemVo = new OrderItemVo();
            orderItemVo = formOrderItemVo(orderItem);
            orderItemVoList.add(orderItemVo);
        }
        orderVo.setOrderItemVoList(orderItemVoList);


//        private String imageHost;
        orderVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));


//        private String paymentTime;
//        private String sendTime;
//        private String endTime;
//        private String closeTime;
//        private String createTime;
        orderVo.setPaymentTime(DateTimeUtil.dateToStr(order.getPaymentTime()));
        orderVo.setSendTime(DateTimeUtil.dateToStr(order.getSendTime()));
        orderVo.setEndTime(DateTimeUtil.dateToStr(order.getEndTime()));
        orderVo.setCreateTime(DateTimeUtil.dateToStr(order.getCreateTime()));
        orderVo.setCloseTime(DateTimeUtil.dateToStr(order.getCloseTime()));
        return orderVo;

    }

    private OrderItemVo formOrderItemVo(OrderItem orderItem) {
        OrderItemVo orderItemVo = new OrderItemVo();
//        private Long orderNo;
        orderItemVo.setOrderNo(orderItem.getOrderNo());
//        private Integer productId;
        orderItemVo.setProductId(orderItem.getProductId());
//        private String productName;
        orderItemVo.setProductName(orderItem.getProductName());
//        private String productImage;
        orderItemVo.setProductImage(orderItem.getProductImage());
//        private BigDecimal currentUnitPrice;
        orderItemVo.setCurrentUnitPrice(orderItem.getCurrentUnitPrice());
//        private Integer quantity;
        orderItemVo.setQuantity(orderItem.getQuantity());
//        private BigDecimal totalPrice;
        orderItemVo.setTotalPrice(orderItem.getTotalPrice());
//        private String createTime;
        orderItemVo.setCreateTime(DateTimeUtil.dateToStr(orderItem.getCreateTime()));
        return orderItemVo;
    }

    private ShippingVo formShippingVO(Shipping shipping) {
        ShippingVo shippingVo = new ShippingVo();
        //        private String receiverName;
        shippingVo.setReceiverName(shipping.getReceiverName());
//        private String receiverPhone;
        shippingVo.setReceiverPhone(shipping.getReceiverPhone());
//        private String receiverMobile;
        shippingVo.setReceiverMobile(shipping.getReceiverMobile());
//        private String receiverProvince;
        shippingVo.setReceiverProvince(shipping.getReceiverProvince());
//        private String receiverCity;
        shippingVo.setReceiverCity(shipping.getReceiverCity());
//        private String receiverDistrict;
        shippingVo.setReceiverDistrict(shipping.getReceiverDistrict());
//        private String receiverAddress;
        shippingVo.setReceiverAddress(shipping.getReceiverAddress());
//        private String receiverZip;
        shippingVo.setReceiverZip(shipping.getReceiverZip());
        return shippingVo;
    }

    private void redueProductStock(List<OrderItem> orderItemList) {
        for (OrderItem orderItem : orderItemList) {
            Product product = productMapper.selectByPrimaryKey(orderItem.getProductId());
            product.setStock(product.getStock() - orderItem.getQuantity());
            productMapper.updateByPrimaryKeySelective(product);
        }
    }

    private void clearCart(List<Cart> cartList) {

        for (Cart cartItem : cartList) {
            cartMapper.deleteByPrimaryKey(cartItem.getId());
        }
    }

    private Order formOrder(Integer userId, Integer shippingId, BigDecimal payment) {
        Order order = new Order();
        long orderNo = this.generateOrderNo();
        order.setOrderNo(orderNo);
        order.setPostage(0);
        order.setPayment(payment);
        order.setPaymentType(Const.PaymentTypeEnum.ONLINE_PAY.getCode());
        order.setStatus(Const.OrderStatusEnum.NO_PAY.getCode());

        order.setUserId(userId);
        order.setShippingId(shippingId);

//        private Long orderNo;
//        private Integer postage;
//        private Integer paymentType;
//        private BigDecimal payment;
//        private Integer status;

//        private Integer userId;
//        private Integer shippingId;
        int rowCount = orderMapper.insert(order);
        if (rowCount > 0) {
            return order;
        }
        return null;
    }

    private long generateOrderNo() {
        long currentTime = System.currentTimeMillis();
        return currentTime + new Random().nextInt(100);
    }

    private BigDecimal getOrderTotalPrice(List<OrderItem> orderItemList) {
        BigDecimal payment = new BigDecimal("0");
        for (OrderItem orderItem : orderItemList) {
            payment = BigDecimalUtil.add(payment.doubleValue(), orderItem.getTotalPrice().doubleValue());
        }
        return payment;

    }

    private ServerResponse formOrderItem(Integer userId, List<Cart> cartList) {
        List<OrderItem> orderItemList = Lists.newArrayList();
        if (CollectionUtils.isEmpty(cartList)) {
            return ServerResponse.createByErrorMessage("购物车为空");
        }
        for (Cart cart : cartList) {
            OrderItem orderItem = new OrderItem();
            Product product = productMapper.selectByPrimaryKey(cart.getProductId());
            if (Const.ProductStatusEnum.ON_SALE.getCode() != product.getStatus()) {
                return ServerResponse.createByErrorMessage("商品" + product.getName() + "不是在线购买状态");
            }
            if (product.getStock() < cart.getQuantity()) {
                return ServerResponse.createByErrorMessage("商品" + product.getName() + "库存不足");
            }
//        private Integer id;
//        private Long orderNo;

//        private Integer productId;
            orderItem.setProductId(product.getId());
//        private String productName;
            orderItem.setProductName(product.getName());
//        private String productImage;
            orderItem.setProductImage(product.getMainImage());
//        private BigDecimal currentUnitPrice;
            orderItem.setCurrentUnitPrice(product.getPrice());
//        private Integer quantity;
            orderItem.setQuantity(cart.getQuantity());
//        private BigDecimal totalPrice;
            orderItem.setTotalPrice(BigDecimalUtil.mul(product.getPrice().doubleValue(), cart.getQuantity()));
//        private Date createTime;
//        private Date updateTime;

//        private Integer userId;
            orderItem.setUserId(cart.getUserId());
            orderItemList.add(orderItem);

        }
        return ServerResponse.createBySuccess(orderItemList);


    }


    //backend
    @Override
    public ServerResponse<PageInfo> manageList(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Order> orderList = orderMapper.selectAll();
        List<OrderVo> orderVoList = formOrderVoList(orderList, null);
        PageInfo pageResult = new PageInfo(orderList);
        pageResult.setList(orderVoList);
        return ServerResponse.createBySuccess(pageResult);
    }

    @Override
    public ServerResponse<OrderVo> orderdetail(Long orderNo) {
        OrderVo orderVo = new OrderVo();
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            return ServerResponse.createByErrorMessage("订单不存在");
        }
        List<OrderItem> orderItemList = orderItemMapper.selectByOrderNo(order.getOrderNo());
        orderVo = formOrderVo(order, orderItemList);
        return ServerResponse.createBySuccess(orderVo);
    }

    @Override
    public ServerResponse<PageInfo> search(Long orderNo, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum,pageSize);
        Order order = orderMapper.selectByOrderNo(orderNo);
        if(order!=null){
            List<OrderItem> orderItemList =orderItemMapper.selectByOrderNo(orderNo);
            OrderVo orderVo=formOrderVo(order,orderItemList);
            PageInfo pageInfo =new PageInfo(Lists.newArrayList(order));
            pageInfo.setList(Lists.newArrayList(orderVo));
            return ServerResponse.createBySuccess(pageInfo);
        }
        return ServerResponse.createByErrorMessage("订单不存在");

    }


}

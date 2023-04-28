package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.*;
import com.itheima.reggie.mapper.OrderMapper;
import com.itheima.reggie.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Orders> implements OrderService {
    @Autowired
    private ShoppingCartService shoppingCartService;

    @Autowired
    private OrderDetailService orderDetailService;

    @Autowired
    private AddressBookService addressBookService;

    @Autowired
    private UserService userService;

    // 用户下单
    @Transactional
    @Override
    public void submit(Orders orders) {
        // 获得当前用户id
        Long userId = BaseContext.getCurrentId();
        // 查询当前用户的购物车数据
        LambdaQueryWrapper<ShoppingCart> lqw = new LambdaQueryWrapper<>();
        lqw.eq(ShoppingCart::getUserId, userId);
        List<ShoppingCart> shoppingCartList = shoppingCartService.list(lqw);
        if (shoppingCartList == null || shoppingCartList.size() == 0) throw new CustomException("购物车为空，不能下单");

        // 查询用户数据
        User user = userService.getById(userId);

        // 查询订单数据
        Long addressBookId = orders.getAddressBookId();
        log.info("addressBookId={}", addressBookId);
        AddressBook addressBook = addressBookService.getById(addressBookId);
        if (addressBook == null) throw new CustomException("用户地址信息有误，不能下单");

        // 向订单表插入数据，一条数据
        long orderId = IdWorker.getId();// 订单号

        // 本信息
        orders.setId(orderId);
        orders.setNumber(String.valueOf(orderId)); // 订单号
        orders.setOrderTime(LocalDateTime.now());
        orders.setCheckoutTime(LocalDateTime.now());
        orders.setStatus(2);

        // 计算购物车总价格  构建订单明细列表
        List<OrderDetail> orderDetailList = new ArrayList<>();
        /*BigDecimal amount = new BigDecimal(0);
        for (ShoppingCart shoppingCart : shoppingCartList) {
            BigDecimal price = shoppingCart.getAmount();
            BigDecimal number = new BigDecimal(shoppingCart.getNumber());
            amount = amount.add(price.multiply(number));
        }*/

        AtomicInteger amount = new AtomicInteger(0);
        for (ShoppingCart shoppingCart : shoppingCartList) {
            OrderDetail orderDetail = new OrderDetail();

            orderDetail.setOrderId(orders.getId());
            orderDetail.setNumber(shoppingCart.getNumber());
            orderDetail.setDishFlavor(shoppingCart.getDishFlavor());
            orderDetail.setDishId(shoppingCart.getDishId());
            orderDetail.setSetmealId(shoppingCart.getSetmealId());
            orderDetail.setName(shoppingCart.getName());
            orderDetail.setImage(shoppingCart.getImage());
            orderDetail.setAmount(shoppingCart.getAmount());

            amount.addAndGet(shoppingCart.getAmount().multiply(new BigDecimal(shoppingCart.getNumber())).intValue());
            orderDetailList.add(orderDetail);
        }

        // 设置总价
        orders.setAmount(new BigDecimal(amount.get()));

        // 地址信息
        orders.setAddress(addressBook.getDetail());
        orders.setConsignee(addressBook.getConsignee());
        orders.setPhone(addressBook.getPhone());
        orders.setAddress((addressBook.getProvinceName() == null ? "" : addressBook.getProvinceName())
                + (addressBook.getCityName() == null ? "" : addressBook.getCityName())
                + (addressBook.getDistrictName() == null ? "" : addressBook.getDistrictName())
                + (addressBook.getDetail() == null ? "" : addressBook.getDetail())
        );

        // 用户信息
        orders.setUserId(userId);
        orders.setUserName(user.getName());


        this.save(orders);

        // 向订单明细表插入数据，可能是多条数据
        orderDetailService.saveBatch(orderDetailList);

        // 清空购物车数据
        shoppingCartService.remove(lqw);
    }
}

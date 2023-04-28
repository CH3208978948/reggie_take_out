package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Orders;
import com.itheima.reggie.service.OrderService;
import com.itheima.reggie.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Slf4j
@RestController
@RequestMapping("/order")
public class OrderController {
    @Autowired
    private OrderService orderService;

    @Autowired
    private ShoppingCartService shoppingCartService;

    // 提交订单（增加到库中）
    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders orders) {
        orderService.submit(orders);
        return R.success("下单成功");
    }

    // 修改订单状态
    @PutMapping
    public R<String> update(@RequestBody Orders orders) {
        orderService.updateById(orders);
        return R.success("订单状态修改成功");
    }

    // 获取页面当前用户订单
    @GetMapping("/userPage")
    public R<Page<Orders>> list(int page, int pageSize) {
        Page<Orders> pageInfo = new Page<>(page, pageSize);
        LambdaQueryWrapper<Orders> lqw = new LambdaQueryWrapper<>();
        lqw.eq(Orders::getUserId, BaseContext.getCurrentId());
        lqw.orderByDesc(Orders::getOrderTime);

        orderService.page(pageInfo, lqw);

        return R.success(pageInfo);
    }

    // 获取页面所有订单
    @GetMapping("/page")
    public R<Page<Orders>> listPage(int page, int pageSize, Long number, String beginTime, String endTime) {
        LambdaQueryWrapper<Orders> lqw = new LambdaQueryWrapper<>();

        log.info("Long={}", number);
        lqw.like(number != null, Orders::getNumber, number);
        lqw.orderByDesc(Orders::getOrderTime);

        if (beginTime != null && endTime != null) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
            Date beginDate = null;
            Date endDate = null;
            try {
                beginDate = simpleDateFormat.parse(beginTime);
                endDate = simpleDateFormat.parse(endTime);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            lqw.between(Orders::getOrderTime, beginDate, endDate);
        }

        Page<Orders> pageInfo = new Page<>(page, pageSize);

        orderService.page(pageInfo, lqw);

        return R.success(pageInfo);
    }
}

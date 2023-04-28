package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.ShoppingCart;
import com.itheima.reggie.service.ShoppingCartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/shoppingCart")
public class ShoppingCartController {
    @Autowired
    private ShoppingCartService shoppingCartService;

    @Value("${global_name.user_id}")
    private String gUId;

    // 查询购物车列表
    @GetMapping("/list")
    public R<List<ShoppingCart>> list(HttpSession session) {
        LambdaQueryWrapper<ShoppingCart> lqw = new LambdaQueryWrapper<>();
        lqw.eq(ShoppingCart::getUserId, session.getAttribute(gUId));
        lqw.orderByDesc(ShoppingCart::getCreateTime);
        return R.success(shoppingCartService.list(lqw));
    }

    // 添加至购物车
    @PostMapping("/add")
    public R<String> save(@RequestBody ShoppingCart shoppingCart, HttpSession session) {
        // shoppingCart.setUserId((Long) session.getAttribute(gUId));
        Long userId = BaseContext.getCurrentId();
        shoppingCart.setUserId(userId);


        // 获取当前 套餐或菜品 如果存在  则在原先的数量上加1 反之添加
        LambdaQueryWrapper<ShoppingCart> lqw = new LambdaQueryWrapper<>();
        lqw.eq(ShoppingCart::getUserId, userId);
        if (shoppingCart.getDishId() != null) {
            lqw.eq(ShoppingCart::getDishId, shoppingCart.getDishId());
        } else {
            lqw.eq(ShoppingCart::getSetmealId, shoppingCart.getSetmealId());
        }

        ShoppingCart queryShoppingCart = shoppingCartService.getOne(lqw);
        if (queryShoppingCart != null) {
            queryShoppingCart.setNumber(queryShoppingCart.getNumber() + 1);

            shoppingCartService.updateById(queryShoppingCart);
            return R.success("购物车添加成功");
        }

        shoppingCart.setCreateTime(LocalDateTime.now());
        shoppingCartService.save(shoppingCart);
        return R.success("加入购物车成功");
    }

    // 从购物车删除
    @PostMapping("/sub")
    public R<String> sub(@RequestBody ShoppingCart shoppingCart, HttpSession session) {
        Long userId = BaseContext.getCurrentId();
        shoppingCart.setUserId(userId);

        // 从数据中查找当前 餐品或套餐  存在且数量 大于1 则直接减1 否则
        LambdaQueryWrapper<ShoppingCart> lqw = new LambdaQueryWrapper<>();
        lqw.eq(ShoppingCart::getUserId, userId);

        if (shoppingCart.getDishId() != null) {
            lqw.eq(ShoppingCart::getDishId, shoppingCart.getDishId());
        } else {
            lqw.eq(ShoppingCart::getSetmealId, shoppingCart.getSetmealId());
        }

        ShoppingCart queryShoppingCart = shoppingCartService.getOne(lqw);
        if (queryShoppingCart.getNumber() > 1) {
            queryShoppingCart.setNumber(queryShoppingCart.getNumber() - 1);

            shoppingCartService.updateById(queryShoppingCart);
            return R.success("购物车修改成功");
        }

        // 将获取到的符合条件的套餐且只有一个的时候 删除该套餐
        shoppingCartService.removeById(queryShoppingCart);

        return R.success("删除购物车成功");
    }

    // 清空购物车
    @DeleteMapping("/clean")
    public R<String> clean(HttpSession session) {
        LambdaQueryWrapper<ShoppingCart> lqw = new LambdaQueryWrapper<>();
        lqw.eq(ShoppingCart::getUserId, session.getAttribute(gUId));

        shoppingCartService.remove(lqw);
        return R.success("购物车清空成功");
    }

}

package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.mapper.CategoryMapper;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishService;
import com.itheima.reggie.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {
    @Autowired
    private DishService dishService;
    @Autowired
    private SetmealService setMealService;

    // 自定义的删除方法  删除前进行判断
    @Override
    public void remove(Long id) {
        // 查询当前分类是否关联了菜品
        LambdaQueryWrapper<Dish> lqw = new LambdaQueryWrapper<>();
        lqw.eq(Dish::getCategoryId, id);
        int count = dishService.count(lqw);

        if (count > 0) {
            // 已经关联菜品，抛出一个业务异常
            throw new CustomException("当前分类下关联了菜品，不能删除");
        };

        // 查询当前分类是否关联了套餐
        LambdaQueryWrapper<Setmeal> lqw2 = new LambdaQueryWrapper<>();
        lqw2.eq(Setmeal::getCategoryId, id);
        count = setMealService.count(lqw2);

        if (count > 0) {
            // 已经关联套餐，抛出一个业务异常
            throw new CustomException("当前分类下关联套餐，不能删除");
        };


        // 正常删除分类【此处是 BaseMapper提供的 removeById】
        removeById(id);
    }
}

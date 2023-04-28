package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishDto;
import com.itheima.reggie.entity.SetmealDto;

public interface DishService extends IService<Dish> {
    // 新增菜品，同时插入菜品对应的口味数据，需要操作两张表，dish，dish_flavor
    void saveWithFlavor(DishDto dishDto);

    // 获取页面菜品，同时插入菜品对应的口味数据、套餐名称
    Page<DishDto> getPageWithFlavors(int page, int pageSize, String name);

    // 根据id获取菜品，同时携带口味数据
    DishDto getDishWithFlavors(Long id);

    // 更新菜品，同时更新口味
    void updateDishWithFlavors(DishDto dishDto);
}

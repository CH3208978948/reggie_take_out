package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishDto;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@RestController
@Slf4j
@RequestMapping("/dish")
public class DishController {
    @Autowired
    private DishService dishService;
    @Autowired
    private DishFlavorService dishFlavorService;
    @Autowired
    private CategoryService categoryService;

    // 使用redis 进行优化
    @Autowired
    private RedisTemplate redisTemplate;

    // 保存菜品
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto) {
        dishService.saveWithFlavor(dishDto);
        // 清理所有菜品的缓存数据
        Set keys = redisTemplate.keys("dish_" + dishDto.getCategoryId() + "_1");
        redisTemplate.delete(keys);

        return R.success("新增菜品成功");
    }

    // 根据id查找菜品
    @GetMapping("/{id}")
    public R<DishDto> getById(@PathVariable Long id) {
        DishDto dishDto = dishService.getDishWithFlavors(id);
        // 查找categoryName 并封装进 dishDto中
        // dishDto.setCategoryName(categoryService.getById(dishDto.getCategoryId()).getName());

        // log.info(dishDto.toString());

        return R.success(dishDto);
    }

    // 查询页面菜品数据
    @GetMapping("/page")
    public R<Page<DishDto>> page(int page, int pageSize, String name) {
        Page<DishDto> dishDtoPage = dishService.getPageWithFlavors(page, pageSize, name);

        return R.success(dishDtoPage);
    }

    // 查询菜品列表
    @GetMapping("/list")
    public R<List<DishDto>> list(Dish dish) {
        List<DishDto> dishDtoList = null;

        // 动态构造key
        String key = "dish_" + dish.getCategoryId() + "_" + dish.getStatus();

        // 先从redis中获取缓存数据
        dishDtoList = (List<DishDto>) redisTemplate.opsForValue().get(key);
        // 如果存在，直接返回，无需查询数据库
        if (dishDtoList != null) return R.success(dishDtoList);
        // 如果不存在，需要查询数据库，将查询到的菜品数据缓存到redis

        /*dishDtoList = redisCacheDishList(dish);
        if (dishDtoList != null) return R.success(dishDtoList);*/
        // 如果不存在，需要查询数据库，将查询到的菜品数据缓存到redis*/

        LambdaQueryWrapper<Dish> lqw = new LambdaQueryWrapper<>();
        lqw.eq(Dish::getCategoryId, dish.getCategoryId());
        // 只查询（起售状态）的菜品
        lqw.eq(Dish::getStatus, 1);
        List<Dish> dishes = dishService.list(lqw);

        // 将数据注入dishDto
        dishDtoList = new ArrayList<>();
        // 查询口味
        LambdaQueryWrapper<DishFlavor> lqwF = new LambdaQueryWrapper<>();
        for (Dish dishI : dishes) {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(dishI, dishDto);

            lqwF.clear();
            lqwF.eq(DishFlavor::getDishId, dishI.getId());
            // 将口味注入
            dishDto.setFlavors(dishFlavorService.list(lqwF));

            dishDtoList.add(dishDto);
        }

        // 如果不存在，需要查询数据库，将查询到的菜品数据缓存到redis
        redisTemplate.opsForValue().set(key, dishDtoList, 60, TimeUnit.MINUTES);

        return R.success(dishDtoList);
    }

    // 修改售卖状态
    @PostMapping("/status/{status}")
    public R<String> updateStatus(@PathVariable Integer status, Long[] ids) {
        Dish dish = new Dish();
        dish.setStatus(status);

        for (Long dishId : ids) {
            dish.setId(dishId);

            dishService.updateById(dish);

            // 清理所有菜品的缓存数据
            dish = dishService.getById(dishId);
            Set keys = redisTemplate.keys("dish_" + dish.getCategoryId() + "_1");
            redisTemplate.delete(keys);
        }

        return R.success("菜品状态修改成功");
    }

    // 更新菜品
    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto) {
        dishService.updateDishWithFlavors(dishDto);

        // 清理所有菜品的缓存数据
        Set keys = redisTemplate.keys("dish_" + dishDto.getCategoryId() + "_1");
        redisTemplate.delete(keys);

        return R.success("修改成功");
    }

    // 删除菜品
    @DeleteMapping
    public R<String> delete(Long[] ids) {
        LambdaQueryWrapper<DishFlavor> lqw = new LambdaQueryWrapper<>();
        for (Long dishId : ids) {
            dishService.removeById(dishId);
            lqw.clear();
            lqw.eq(DishFlavor::getDishId, dishId);
            dishFlavorService.remove(lqw);

            // 清理所有菜品的缓存数据
            Set keys = redisTemplate.keys("dish_" + dishService.getById(dishId).getCategoryId() + "_1");
            redisTemplate.delete(keys);
        }
        return R.success("菜品删除成功");
    }
}

package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.*;
import com.itheima.reggie.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@Slf4j
@RequestMapping("/setmeal")
public class SetmealController {
    @Autowired
    private DishService dishService;
    @Autowired
    private DishFlavorService dishFlavorService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private SetmealService setmealService;
    @Autowired
    private SetmealDishService setmealDishService;

    // 删除缓存中 所有的套餐 setmealCache中的所有的属性 allEntries 默认为false  手动添加剂为true即可
    @CacheEvict(value = "setmealCache", allEntries = true)
    // 保存套餐
    @PostMapping
    public R<String> save(@RequestBody SetmealDto setmealDto) {
        // 将setmeal的信息存入
        setmealService.save(setmealDto);
        // 存入setmealDish的信息
        for (SetmealDish setmealDish : setmealDto.getSetmealDishes()) {
            setmealDish.setSetmealId(setmealDto.getId());
        }
        setmealDishService.saveBatch(setmealDto.getSetmealDishes());
        return R.success("新增套餐成功");
    }

    // 根据id查找套餐
    @GetMapping("/{id}")
    public R<SetmealDto> getById(@PathVariable Long id) {
        SetmealDto setmealDto = setmealService.getSetmealWithSetmealDish(id);
        return R.success(setmealDto);
    }

    // 查询页面套餐数据
    @GetMapping("/page")
    public R<Page<SetmealDto>> getPage(int page, int pageSize, String name) {
        Page<SetmealDto> setmealDtoPage = setmealService.getPageWithCategoryName(page, pageSize, name);

        return R.success(setmealDtoPage);
    }

    // 删除缓存中 所有的套餐 setmealCache中的所有的属性 allEntries 默认为false  手动添加剂为true即可
    @CacheEvict(value = "setmealCache", allEntries = true)
    // 修改售卖状态
    @PostMapping("/status/{status}")
    public R<String> updateStatus(@PathVariable Integer status, Long[] ids) {
        Setmeal setmeal = new Setmeal();
        setmeal.setStatus(status);

        for (Long dishId : ids) {
            setmeal.setId(dishId);
            setmealService.updateById(setmeal);
        }

        return R.success("套餐状态修改成功");
    }

    // 删除缓存中 所有的套餐 setmealCache中的所有的属性 allEntries 默认为false  手动添加剂为true即可
    @CacheEvict(value = "setmealCache", allEntries = true)
    // 更新套餐
    @PutMapping
    public R<String> update(@RequestBody SetmealDto setmealDto) {
        setmealService.updateSetmealWithDish(setmealDto);
        return R.success("修改成功");
    }

    // 删除缓存中 所有的套餐 setmealCache中的所有的属性 allEntries 默认为false  手动添加剂为true即可
    @CacheEvict(value = "setmealCache", allEntries = true)
    // 删除套餐
    @DeleteMapping
    public R<String> delete(Long[] ids) {
        setmealService.deleteByIds(ids);

        return R.success("套餐删除成功");
    }

    // 将查询到的缓存数据放到redis中
    @Cacheable(value = "setmealCache", key = "#setmeal.categoryId + '_' + #setmeal.status")
    // 查询套餐列表
    @GetMapping("/list")
    public R<List<Setmeal>> list(Setmeal setmeal) {
        System.out.println(setmeal.getCategoryId());
        System.out.println(setmeal.getStatus());

        LambdaQueryWrapper<Setmeal> lqw = new LambdaQueryWrapper<>();
        lqw.eq(Setmeal::getCategoryId, setmeal.getCategoryId());
        lqw.eq(Setmeal::getStatus, setmeal.getStatus());

        List<Setmeal> setmealList = setmealService.list(lqw);

        return R.success(setmealList);
    }
}

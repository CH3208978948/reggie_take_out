package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.*;
import com.itheima.reggie.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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

    // 更新套餐
    @PutMapping
    public R<String> update(@RequestBody SetmealDto setmealDto) {
        setmealService.updateSetmealWithDish(setmealDto);
        return R.success("修改成功");
    }

    // 删除套餐
    @DeleteMapping
    public R<String> delete(Long[] ids) {
        setmealService.deleteByIds(ids);

        return R.success("套餐删除成功");
    }

    // 查询套餐列表
    @GetMapping("/list")
    public R<List<Setmeal>> list(String categoryId, int status) {
        System.out.println(categoryId);
        System.out.println(status);

        LambdaQueryWrapper<Setmeal> lqw = new LambdaQueryWrapper<>();
        lqw.eq(Setmeal::getCategoryId, categoryId);
        lqw.eq(Setmeal::getStatus, status);

        List<Setmeal> setmealList = setmealService.list(lqw);

        return R.success(setmealList);
    }
}

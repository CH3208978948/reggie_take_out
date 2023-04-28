package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.entity.SetmealDish;
import com.itheima.reggie.entity.SetmealDto;
import com.itheima.reggie.mapper.SetmealMapper;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private SetmealDishService setmealDishService;

    @Override
    public Page<SetmealDto> getPageWithCategoryName(int page, int pageSize, String name) {
        Page<SetmealDto> setmealDtoPage = new Page<>();

        // 获取setmeal 当前页面的数据
        Page<Setmeal> setmealPage = new Page<>(page, pageSize);
        LambdaQueryWrapper<Setmeal> lqw = new LambdaQueryWrapper<>();
        lqw.like(Strings.isNotEmpty(name), Setmeal::getName, name);
        lqw.orderByDesc(Setmeal::getUpdateTime);
        this.page(setmealPage, lqw);

        // 将setmealPage 的其他数据 封装进setmealDtoPage中
        BeanUtils.copyProperties(setmealPage, setmealDtoPage, "records");

        // 设置setmealDto 以及特有的categoryName属性
        List<SetmealDto> setmealDtoList= new ArrayList<>();
        for (Setmeal setmeal : setmealPage.getRecords()) {
            SetmealDto setmealDto = new SetmealDto();
            BeanUtils.copyProperties(setmeal, setmealDto);
            setmealDto.setCategoryName(categoryService.getById(setmeal.getCategoryId()).getName());
            setmealDtoList.add(setmealDto);
        }
        // 设置进 records中
        setmealDtoPage.setRecords(setmealDtoList);

        return setmealDtoPage;
    }

    // 根据id查找套餐信息，携带套餐菜品信息
    @Override
    public SetmealDto getSetmealWithSetmealDish(Long id) {
        SetmealDto setmealDto = new SetmealDto();
        // 放入setmeal基本信息
        BeanUtils.copyProperties(this.getById(id), setmealDto);
        // 放入setmealDish信息
        LambdaQueryWrapper<SetmealDish> lqw = new LambdaQueryWrapper<>();
        lqw.eq(SetmealDish::getSetmealId, setmealDto.getId());
        setmealDto.setSetmealDishes(setmealDishService.list(lqw));

        return setmealDto;
    }

    // 更新套餐，同时更新菜品列表
    @Override
    public void updateSetmealWithDish(SetmealDto setmealDto) {
        // 更新套餐
        this.updateById(setmealDto);

        // 更新套餐菜品
        LambdaQueryWrapper<SetmealDish> lqw = new LambdaQueryWrapper<>();
        lqw.eq(SetmealDish::getSetmealId, setmealDto.getId());
        setmealDishService.remove(lqw);
        for (SetmealDish setmealDish : setmealDto.getSetmealDishes()) {
            setmealDish.setSetmealId(setmealDto.getId());
        }
        setmealDishService.saveBatch(setmealDto.getSetmealDishes());
    }

    // 删除非售卖中的套餐及菜品
    @Override
    public void deleteByIds(Long[] ids) {
        LambdaQueryWrapper<SetmealDish> setmealDishLQW = new LambdaQueryWrapper<>();
        LambdaQueryWrapper<Setmeal> setmealLQW = new LambdaQueryWrapper<>();

        setmealLQW.in(Setmeal::getId, ids);
        // 正在售卖的套餐不允许删除
        setmealLQW.eq(Setmeal::getStatus, 1);
        if (this.count(setmealLQW) > 0) throw new CustomException("套餐正在售卖中，不能删除");

        // 删除套餐
        setmealLQW.clear();
        setmealLQW.in(Setmeal::getId, ids);
        this.remove(setmealLQW);

        // 删除菜品
        setmealDishLQW.in(SetmealDish::getSetmealId, ids);
        setmealDishService.remove(setmealDishLQW);
    }
}

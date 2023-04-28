package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishDto;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.mapper.DishMapper;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {
    @Autowired
    private DishFlavorService dishFlavorService;
    @Autowired
    private CategoryService categoryService;

    // 保存菜品  包括口味信息
    @Override
    public void saveWithFlavor(DishDto dishDto) {
        // 保存菜品的基本信息到菜品表dish
        this.save(dishDto);

        // 执行添加操作时  算法在内部添加了一个id  添加操作过后  就有id了
        Long dishId = dishDto.getId(); // 菜品id

        for (DishFlavor flavor : dishDto.getFlavors()) {
            flavor.setDishId(dishId);
        }

        // 保存菜品口味数据到菜品口味表dish_flavor
        dishFlavorService.saveBatch(dishDto.getFlavors());
    }

    // 获取页面菜品  携带口味信息、菜品分类
    @Override
    public Page<DishDto> getPageWithFlavors(int page, int pageSize, String name) {
        // log.info("page ===> " + page + "\npageSize ===> " + pageSize);

        // 构造分页构造器
        // dish数据
        Page<Dish> pageInfo = new Page<>(page, pageSize);
        // dishDto数据  用于添加 特有categoryName属性值
        Page<DishDto> dishDtoPage = new Page<>();
        // 构造条件构造器
        LambdaQueryWrapper<Dish> lqw = new LambdaQueryWrapper<>();
        lqw.like(Strings.isNotEmpty(name), Dish::getName, name);
        lqw.orderByDesc(Dish::getSort);
        // 添加排序条件
        lqw.orderByDesc(Dish::getUpdateTime);

        // 执行查询  查询所有 dish数据
        this.page(pageInfo, lqw);

        // 对象拷贝  将除了 dish集合之外的数据 复制进 dishDto中
        // 【因为dishDto的records集合为 dishDto类型集合 需要后面手动添加特有属性 categoryName并放进集合中】
        BeanUtils.copyProperties(pageInfo, dishDtoPage, "records");
        List<Dish> records = pageInfo.getRecords();

        // 用来装dishDto
        List<DishDto> dishDtoList = new ArrayList<>();
        // 获取查询到的dish
        for (Dish dish : records) {
            // 创建dishDto对象
            DishDto dishDto = new DishDto();
            // 将原有的dish属性 复制进dishDto中
            BeanUtils.copyProperties(dish, dishDto);
            // 设置dishDto特有属性 categoryName
            dishDto.setCategoryName(categoryService.getById(dish.getCategoryId()).getName());
            // 将 封装好的dishDto放进 集合中
            dishDtoList.add(dishDto);
        }

        // 将dishDto集合设置为 page的records
        dishDtoPage.setRecords(dishDtoList);

        return dishDtoPage;
    }

    // 根据单个id获取菜品 携带口味信息
    // 【页面会根据获取到的菜品分类（/category/list?type=1）数据  根据categoryId 获取对应name，所以此处无需获取categoryName】
    @Override
    public DishDto getDishWithFlavors(Long id) {
        DishDto dishDto = new DishDto();
        // 查找dish数据 并封装进 dishDto中
        BeanUtils.copyProperties(this.getById(id), dishDto);
        // 查找dishFlavor数据 并封装进 dishDto中
        LambdaQueryWrapper<DishFlavor> lqw = new LambdaQueryWrapper<>();
        lqw.eq(DishFlavor::getDishId, id);

        dishDto.setFlavors(dishFlavorService.list(lqw));

        return dishDto;
    }

    @Override
    public void updateDishWithFlavors(DishDto dishDto) {
        // 更新dish表基本信息
        this.updateById(dishDto);

        // 添加口味之前将 库里的相应菜品口味全部删除  因为未提交的口味未能山删除
        LambdaQueryWrapper<DishFlavor> lqw = new LambdaQueryWrapper<>();
        lqw.eq(DishFlavor::getDishId, dishDto.getId());
        dishFlavorService.remove(lqw);

        for (DishFlavor flavor : dishDto.getFlavors()) {
            if (flavor.getDishId() == null) flavor.setDishId(dishDto.getId());
        }

        // 保存口味数据
        dishFlavorService.saveBatch(dishDto.getFlavors());
    }
}

package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/category")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    // 保存分类
    @PostMapping
    public R<String> save(@RequestBody Category category) {
        System.out.println(category);
        categoryService.save(category);
        return R.success("保存成功");
    }

    // 查询分类数据
    @GetMapping("/page")
    public R<Page<Category>> getPage(int page, int pageSize) {
        log.info("page ===> " + page + "\npageSize ===> " + pageSize);

        // 构造分页构造器
        Page<Category> pageInfo = new Page<>(page, pageSize);
        // 构造条件构造器
        LambdaQueryWrapper<Category> lqw = new LambdaQueryWrapper<>();
        lqw.orderByDesc(Category::getSort);
        // 添加排序条件
        lqw.orderByDesc(Category::getUpdateTime);

        // 执行查询
        categoryService.page(pageInfo, lqw);
        return R.success(pageInfo);
    }

    // 更新分类
    @PutMapping
    public R<String> update(@RequestBody Category category) {
        categoryService.updateById(category);
        return R.success("修改成功");
    }

    // 删除分类
    @DeleteMapping
    public R<String> delete(Long ids) {
        // categoryService.removeById(ids);
        categoryService.remove(ids);
        return R.success("测试删除");
    }

    // 查找所有菜品
    @GetMapping("/list")
    public R<List<Category>> list(Category category) {
        // 条件构造器
        LambdaQueryWrapper<Category> lqw = new LambdaQueryWrapper<>();
        // 添加条件
        lqw.eq(category.getType() != null, Category::getType, category.getType());
        // 添加排序条件
        lqw.orderByAsc(Category::getSort).orderByDesc(Category::getUpdateTime);
        List<Category> categories = categoryService.list(lqw);
        return R.success(categories);
    }
}

package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.entity.SetmealDto;

public interface SetmealService extends IService<Setmeal> {
    // 查询页面套餐，携带分类名称
    Page<SetmealDto> getPageWithCategoryName(int page, int pageSize, String name);

    // 根据id查找套餐信息，携带套餐菜品信息
    SetmealDto getSetmealWithSetmealDish(Long id);

    // 更新套餐，同时更新套餐菜品信息
    void updateSetmealWithDish(SetmealDto setmealDto);

    // 删除非售卖中的套餐，同时删除关联菜品信息
    void deleteByIds(Long[] ids);
}

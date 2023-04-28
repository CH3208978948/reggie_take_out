package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.AddressBook;
import com.itheima.reggie.service.AddressBookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/addressBook")
public class AddressBookController {
    @Autowired
    private AddressBookService addressBookService;

    // 获取当前用户所有地址
    @GetMapping("/list")
    public R<List<AddressBook>> list() {
        LambdaQueryWrapper<AddressBook> lqw = new LambdaQueryWrapper<>();
        lqw.eq(AddressBook::getUserId, BaseContext.getCurrentId());
        return R.success(addressBookService.list(lqw));
    }

    // 获取单个地址
    @GetMapping("{id}")
    public R<AddressBook> selectOne(@PathVariable Long id) {
        return R.success(addressBookService.getById(id));
    }

    // 新增地址
    @PostMapping
    public R<String> save(@RequestBody AddressBook addressBook) {
        addressBook.setUserId(BaseContext.getCurrentId());

        addressBookService.save(addressBook);
        return R.success("地址保存成功");
    }

    // 修改地址
    @PutMapping
    public R<String> update(@RequestBody AddressBook addressBook) {
        addressBookService.updateById(addressBook);

        return R.success("地址修改成功");
    }

    // 删除地址
    @DeleteMapping
    public R<String> delete(Long[] ids) {
        if (ids.length > 0 ) {
            for (Long id : ids) {
                addressBookService.removeById(id);
            }
        }

        return R.success("地址删除成功");
    }

    // 获取默认地址
    @GetMapping("/default")
    public R<AddressBook> getDefault() {
        LambdaQueryWrapper<AddressBook> lqw = new LambdaQueryWrapper<>();
        lqw.eq(AddressBook::getUserId, BaseContext.getCurrentId());
        lqw.eq(AddressBook::getIsDefault, 1);
        return R.success(addressBookService.getOne(lqw));
    }

    // 设置默认地址
    @PutMapping("/default")
    public R<String> updateDefault(@RequestBody AddressBook addressBook) {
        // 删除默认地址
        LambdaUpdateWrapper<AddressBook> luw = new LambdaUpdateWrapper<>();
        luw.eq(AddressBook::getUserId, BaseContext.getCurrentId());
        luw.eq(AddressBook::getIsDefault, 1);
        luw.set(AddressBook::getIsDefault, 0);
        addressBookService.update(luw);

        // 设置默认地址
        addressBook.setIsDefault(1);
        addressBookService.updateById(addressBook);
        return R.success("默认地址设置成功");
    }
}

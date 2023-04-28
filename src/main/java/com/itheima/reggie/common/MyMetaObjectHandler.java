package com.itheima.reggie.common;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;


@Component
@Slf4j
// 自定义元数据对象处理器
public class MyMetaObjectHandler implements MetaObjectHandler {
    // 插入操作自动填充
    @Override
    public void insertFill(MetaObject metaObject) {
        System.out.println("公共字段自动填充[insert]");

        metaObject.setValue("createTime", LocalDateTime.now());
        metaObject.setValue("updateTime", LocalDateTime.now());

        Long empId = BaseContext.getCurrentId();
        log.info(String.valueOf(empId));
        metaObject.setValue("createUser", empId);
        metaObject.setValue("updateUser", empId);
    }

    // 更新操作自动填充
    @Override
    public void updateFill(MetaObject metaObject) {
        System.out.println("线程id:" + Thread.currentThread().getId());

        System.out.println("公共字段自动填充[update]");

        metaObject.setValue("updateTime", LocalDateTime.now());

        metaObject.setValue("updateUser", BaseContext.getCurrentId());
    }
}

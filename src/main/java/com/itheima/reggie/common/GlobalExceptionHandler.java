package com.itheima.reggie.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLIntegrityConstraintViolationException;

// 全局异常处理
@ControllerAdvice(annotations = {RestController.class, Controller.class}) // 扫描指定了这两个直接的类  添加异常处理
@ResponseBody
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public R<String> exceptionHandler(SQLIntegrityConstraintViolationException ex) {
        log.error(ex.getMessage());
        String msg = "未知错误";

        //
        if (ex.getMessage().contains("Duplicate entry")) {
            String[] split = ex.getMessage().split(" ");
            msg = split[2] + "已存在";
        }
        return R.error(msg);
    }

    @ExceptionHandler(CustomException.class)
    public R<String> exceptionHandler(CustomException ex) {
        // log.error(ex.getMessage());
        return R.error(ex.getMessage());
    }
}

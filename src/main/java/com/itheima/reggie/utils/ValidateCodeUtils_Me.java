package com.itheima.reggie.utils;

public class ValidateCodeUtils_Me {
    public static String getAuthCode(int n) {
        // 获取n位授权码
        return (long) (Math.random() * Math.pow(10, n - 1) + 9 * Math.pow(10, n - 1)) + "";
    }
}

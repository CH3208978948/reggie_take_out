package com.itheima.reggie;

import com.itheima.reggie.utils.SendEmailCode;
import net.sf.jsqlparser.expression.DateTimeLiteralExpression;
import org.apache.commons.mail.EmailException;
import org.springframework.util.DigestUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class Test {
    public static void main(String[] args) {
        String s = DigestUtils.md5DigestAsHex("1234567".getBytes());
        System.out.println(s);

        String authCode = (int) (Math.random() * 100000) + 900000 + "";
        System.out.println(authCode);

        authCode = (long) (Math.random() * Math.pow(10, 5) + 9 * Math.pow(10, 5)) + "";
        System.out.println(authCode);

        try {
            SendEmailCode.sendQQEmail("3208978948@qq.com", "Lin", "2309802798@qq.com", "123456", "aewsfzkjbdesdfhh");
        } catch (EmailException e) {
            e.printStackTrace();
        }

        Date date = null;
        try {
            date = new SimpleDateFormat("yy-MM-dd HH:mm:ss").parse("2023-05-22 23:59:59");
        } catch (ParseException e) {
            e.printStackTrace();
        }
        System.out.println(date);

        List.class.cast(null);
    }
}

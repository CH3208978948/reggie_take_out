package com.itheima.reggie.common;


import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;

@Component
@Controller
@Service
public class GlobalName {
    public static GlobalName globalName;

    // 规定好的变量名
    /*@Value("${email.sender}")
    public String USER_ID;
    @Value("${global_name.user_id}")
    public String EMPLOYEE_ID;

    static {
        globalName = new GlobalName();
        System.out.println("USER_ID => " + globalName.USER_ID);
        System.out.println("EMPLOYEE_ID => " + globalName.EMPLOYEE_ID);
    }*/
}

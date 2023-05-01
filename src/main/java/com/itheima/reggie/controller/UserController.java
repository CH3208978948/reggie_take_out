package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.User;
import com.itheima.reggie.service.UserService;
import com.itheima.reggie.utils.SendEmailCode;
import com.itheima.reggie.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.mail.EmailException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {
    @Autowired
    private UserService userService;

    // Redis
    @Autowired
    private RedisTemplate redisTemplate;

    @Value("${email.sender}")
    private String sender;

    @Value("${email.sign}")
    private String sign;

    @Value("${email.auth_code}")
    private String serviceAuthCode;

    @Value("${global_name.employee_id}")
    private String gEmpId;

    @Value("${global_name.user_id}")
    private String gUId;

    // 发送QQ邮箱验证码
    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpSession session) {
        // 获得手机号【黑马】
        // String email = user.getPhone();

        // 获得QQ邮箱
        String email = user.getEmail();
        log.info("email={}", email);

        // 测试期间  暂时不适用验证码【已测试 验证码功能正常使用】
        // if (true) return R.success("验证码发送成功");

        if (StringUtils.isNotEmpty(email)) {
            // 生成随机的4位验证码
            String authCode = ValidateCodeUtils.generateValidateCode(4).toString();


            log.info("code={}", authCode);

            try {
                // 调用QQ邮箱短信服务API完成发送短信
                SendEmailCode.sendQQEmail(sender, sign, email, authCode, serviceAuthCode);
            } catch (EmailException ex) {
                log.warn("邮件服务异常 ===》 UserController Line54");
                return R.error("验证码获取失败，服务器端繁忙，请稍后再试！");
            }


            // 需要将生成的验证码保存到Session
            // session.setAttribute(email, authCode);

            // 将生成的验证码缓存到Redis中，并且设置有效期为5分钟（测试时间30秒）
            // redisTemplate.opsForValue().set(email, authCode, 5, TimeUnit.MINUTES);
            redisTemplate.opsForValue().set(email, authCode, 30, TimeUnit.SECONDS);

            return R.success("验证码获取成功");
        }

        return R.error("验证码获取失败");
    }

    // 验证登录验证码
    @PostMapping("/login")
    public R<User> login(@RequestBody Map map, HttpSession session) {
        //
        // log.info(map.toString());
        // 获取邮箱和验证码
        String email = map.get("email").toString();

        // session中的验证码
        // String authCode = (String) (session.getAttribute(email) == null ? "null" : session.getAttribute(email));

        // 从Redis 中获取缓存的验证码
        String authCode = redisTemplate.opsForValue().get(email) == null ? "null" : (String) redisTemplate.opsForValue().get(email);
        String sendAuthCode = map.get("code").toString();
        if ("null".equals(sendAuthCode)) return R.error("验证码非法");

        //log.info("authCode={}", authCode);
        //log.info("sendCode={}", map.get("code").toString());
        //log.info("compare={}", authCode.equals(map.get("code").toString()));

        // 验证码比对  将提交的验证码与session中存储的验证码进行比对
        if (authCode.equals(sendAuthCode)) {
        // 测试期间  不进行验证码比对【已测试  可正常使用】
        // if (true) {
            // 判断当手机号对应的用户是否为新用户，如果是新用户就自动完成注册
            LambdaQueryWrapper<User> lqw = new LambdaQueryWrapper<>();
            lqw.eq(User::getEmail, email);
            User user = userService.getOne(lqw);

            // 如果用户不存在则注册
            if (user == null) {
                user = new User();
                user.setEmail(email);
                user.setStatus(1);
                userService.save(user);
            }

            // 禁用账户禁止登录
            if (user.getStatus() == 0) {
                return R.error("当前用户禁止登录");
            }

            // session.setAttribute(GlobalName.globalName.USER_ID, user.getId());

            // 用户登录则清除PC 防止进入后台
            // session.removeAttribute(gEmpId);

            session.setAttribute(gUId, user.getId());
            System.out.println("userID" + user.getId());

            // 如果用户登录成功，删除Redis中缓存的验证码
            redisTemplate.delete(email);

            return R.success(user);
        }

        return R.error("验证码错误");
    }

    // 退出登录
    @PostMapping("/loginout")
    public R<String> loginOut(HttpSession httpSession) {
        httpSession.removeAttribute(gUId);
        return R.success("退出登录");
    }
}

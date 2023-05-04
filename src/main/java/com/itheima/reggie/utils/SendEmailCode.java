package com.itheima.reggie.utils;

import com.itheima.reggie.Test;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * 通过QQ邮箱发送验证码
 */
public class SendEmailCode {
    private static String MSG_TEMPLE = "{receiver}，你好!\n" +
            "\n" +
            "<p>我们已收到你要求获得 菩提阁 帐户所用的一次性代码的申请。<p>\n" +
            "\n" +
            "你的一次性代码为: {code}\n" +
            "\n\n" +
            "如果你没有请求此代码，可放心忽略这封电子邮件。别人可能错误地键入了你的电子邮件地址。\n" +
            "\n\n" +
            "谢谢!\n\n" +
            "Lin 菩提阁帐户团队";


    // 指向 resources 下的email 文件
    private static String FILE_PATH = "./src/main/resources/email.html";


    public static void sendQQEmail(String senderQQEmail, String senderName, String receiverQQEmail, String authCode, String serviceAuthCode) throws EmailException {
        Properties p = new Properties();

        SimpleEmail send = new SimpleEmail();

        // 固定值，QQ邮箱服务
        send.setHostName("smtp.qq.com");
        // 固定值，QQ邮箱端口号
        send.setSmtpPort(465);
        send.setCharset("utf-8");
        send.setSSL(true);
        // 接收者的Email
        send.addTo(receiverQQEmail);
        // 参数1：发送者的QQEmail，参数2：发送者显示名字
        send.setFrom(senderQQEmail, senderName);
        // 参数1：发送者的QQEmail，参数2：第一步获取的授权码
        send.setAuthentication(senderQQEmail, serviceAuthCode);
        // 邮件标题
        send.setSubject("你的一次性授权码");
        // 邮件内容
        String msg = null;
        //msg = MSG_TEMPLE;

        try {
            msg = Files.readString(Paths.get(FILE_PATH));
        } catch (IOException e) {
            e.printStackTrace();
        }

        msg = msg.replace("{receiver}", receiverQQEmail);
        msg = msg.replace("{code}", authCode);

        send.setMsg(msg);
        send.send();
    }
}
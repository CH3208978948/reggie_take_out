package com.itheima.reggie.controller;

import com.itheima.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.UUID;

// 文件上传和下载
@Slf4j
@RestController
@RequestMapping("/common")
public class CommonController {
    @Value("${reggie.path}")
    private String basePath;


    // 文件上传
    // 参数名需和 传过来的name  相同才能自动注入
    @RequestMapping("/upload")
    public R<String> upload(MultipartFile file) {
        // file 是一个临时文件，需要转存到指定位置，否则本次请求完成后临时文件会删除
        // log.info(file.getOriginalFilename());

        // 使用UUID重新生成文件名，防止文件名称重复造成文件覆盖
        File aimFile;
        String aimFileName;
        String suffix = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf('.'));
        do {
            aimFileName = UUID.randomUUID().toString() + suffix;
            aimFile = new File(basePath + aimFileName);
        } while (aimFile.exists());

        try {
            // 如果目录结构不存在 则创建该目录结构
            File dir = new File(basePath);
            if (!dir.exists()) dir.mkdirs();

            // 将临时文件转存到指定位置
            file.transferTo(new File(basePath + aimFileName));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return R.success(aimFileName);
    }

    // 文件下载
    @GetMapping("/download")
    public void download(String name, HttpServletResponse response) {
        try {
            // 输入流，通过输入流读取文件内容
            FileInputStream fis = new FileInputStream(basePath + name);
            // 输出流，通过输出流将文件写回浏览器，在浏览器展示图片
            ServletOutputStream outputStream = response.getOutputStream();

            response.setContentType("image/jpeg");

            int len = 0;
            byte[] bytes = new byte[1024];
            while ((len = fis.read(bytes)) != -1) {
                outputStream.write(bytes, 0, len);
                outputStream.flush();
            }

            outputStream.close();
            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}

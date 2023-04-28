package com.itheima.reggie.filter;


import com.alibaba.fastjson.JSON;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/*
 *   检查用户是否已经完成登录
 * */
@WebFilter(filterName = "loginCheckFilter", urlPatterns = "/*")
@Slf4j
public class LoginCheckFilter implements Filter {
    // 路径匹配符，支持通配符
    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Value("${global_name.employee_id}")
    private String gEmpId;

    @Value("${global_name.user_id}")
    private String gUId;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        log.info("拦截器已启动");
        log.info("当前请求id：" + Thread.currentThread().getId());

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        /**
         * 1. 获取本次请求的URI
         * 2. 判断本次请求是否需要处理
         * 3. 如果不需要处理，则直接放行
         * 4. 判断登录状态，如果已登录，则直接放行
         * 5. 如果未登录则返回未登录结果
         */


        String uri = request.getRequestURI();
        // 定义不被拦截的请求
        String[] uris = new String[]{
                // PC端登录 登出功能
                "/employee/login",
                "/employee/logout",

                /*"/backend/**",
                "/front/**",*/

                "/**/login.html",

                "/common/upload",
                "/common/download",
                "/user/sendMsg", // 移动端发送短信
                "/user/login", // 移动端登录
        };

        // 特定请求放行
        if (check(uris, uri)) {
            log.info("特定请求={}放行", uri);
            filterChain.doFilter(request, response);
            return;
        }

        // 静态资源 非html放行
        if (uri.contains(".") && !"html".equals(uri.split("\\.")[uri.split("\\.").length - 1])) {
            filterChain.doFilter(request, response);
            return;
        }

        // 判断是否登录
        Long empId = (Long) request.getSession().getAttribute(gEmpId);
        Long userId = (Long) request.getSession().getAttribute(gUId);

        // 未登录拦截
        if (empId == null && userId == null) {
            // 将未登录用户 转入相对应的登录页面
            checkPathAndUser(empId, userId, uri, response);

            response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
            return;
        }

        // 防止前后端登录后 进入对方的界面
        checkPathAndUser(empId, userId, uri, response);

        BaseContext.setCurrentId(empId != null ? empId : userId);

        filterChain.doFilter(servletRequest, servletResponse);
    }

    // 路径匹配，检查本次请求是否需要放行
    public boolean check(String[] uris, String requestURI) {
        for (String uri : uris) {
            if (PATH_MATCHER.match(uri, requestURI)) {
                // log.info("放行 {}", requestURI);
                return true;
            }
        }
        return false;
    }

    // 防止前后端登录后 进入对方的界面
    public void checkPathAndUser(Long empId, Long userId, String uri, HttpServletResponse response) throws IOException {
        if (userId == null && uri.contains("/front/")) {
            response.sendRedirect("/front/page/login.html");
            return;
        }

        if (empId == null && uri.contains("/backend/")) {
            response.sendRedirect("/backend/page/login/login.html");
            return;
        }
    }


    // MyFunction
//    public void f1(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
//        // log.info("拦截器已启动");
//        HttpServletRequest request = (HttpServletRequest) servletRequest;
//        HttpServletResponse response = (HttpServletResponse) servletResponse;
//        /**
//         * 1. 获取本次请求的URI
//         * 2. 判断本次请求是否需要处理
//         * 3. 如果不需要处理，则直接放行
//         * 4. 判断登录状态，如果已登录，则直接放行
//         * 5. 如果未登录则返回未登录结果
//         */
//
//        // 1.
//        String uri = request.getRequestURI();
//        // 定义不被拦截的请求
//        String[] uris = new String[]{
//                // PC端登录 登出功能
//                // "/employee/login",
//                // "/employee/logout",
//                // 此两个为教程  认为可优化
//                /*"/backend/**",
//                "/front/**",*/
//
//                // 登录前仅仅可进入登录页面
//                /*"/backend/page/login/login.html",
//                "/front/page/login.html",*/
//
//                "/*/login.html",
//                "/*/login",
//
//                "/common/upload",
//                "/common/download",
//                "/user/sendMsg", // 移动端发送短信
//                "/user/login", // 移动端登录
//        };
//
//        // 特定请求放行
//        if (check(uris, uri)) {
//            log.info("特定请求={}放行", uri);
//            filterChain.doFilter(request, response);
//            return;
//        }
//
//        // 静态资源非 html放行
//        log.info("后缀={}", uri.split("\\.")[uri.split("\\.").length - 1]);
//        if (uri.contains(".") && !"html".equals(uri.split("\\.")[uri.split("\\.").length - 1])) {
//            log.info("静态资源非 html放行={}", uri);
//            filterChain.doFilter(request, response);
//            return;
//        }
//
//        // 判断是否登录
//        Long empId = (Long) request.getSession().getAttribute(gEmpId);
//        Long userId = (Long) request.getSession().getAttribute(gUId);
//
//        // 未登录拦截
//        if (empId == null && userId == null) {
//            // 未登录 拦截
//            log.info("未登录html或请求={}被拦截", uri);
//
//            String loginUri = uri.contains("front") ? "/front/page/login.html" : "/backend/page/login/login.html";
//            /*request.getRequestDispatcher(loginUri).forward(request, response);
//            return;*/
//
//            response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
//            return;
//        }
//
//        // 已登录 放行 非特定界面
//        String[] prefixes = {"", ""};
//        if (empId != null) prefixes[0] = ("/backend/**");
//        if (userId != null) prefixes[1] = ("/front/**");
//
//        // 已登录 放行非特定页面 的html
//        if (check(prefixes, uri)) {
//            log.info("非特定界面html{}放行", uri);
//            filterChain.doFilter(request, response);
//            return;
//        }
//    }

    // itheima
//    public void f2(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
//        // log.info("拦截器已启动");
//        HttpServletRequest request = (HttpServletRequest) servletRequest;
//        HttpServletResponse response = (HttpServletResponse) servletResponse;
//        /**
//         * 1. 获取本次请求的URI
//         * 2. 判断本次请求是否需要处理
//         * 3. 如果不需要处理，则直接放行
//         * 4. 判断登录状态，如果已登录，则直接放行
//         * 5. 如果未登录则返回未登录结果
//         */
//
//
//        String uri = request.getRequestURI();
//        // 定义不被拦截的请求
//        String[] uris = new String[]{
//                // PC端登录 登出功能
//                "/employee/login",
//                "/employee/logout",
//
//                "/backend/**",
//                "/front/**",
//
//                "/common/upload",
//                "/common/download",
//                "/user/sendMsg", // 移动端发送短信
//                "/user/login", // 移动端登录
//        };
//
//        // 特定请求放行
//        if (check(uris, uri)) {
//            log.info("特定请求={}放行", uri);
//            filterChain.doFilter(request, response);
//            return;
//        }
//
//        // 判断是否登录
//        Long empId = (Long) request.getSession().getAttribute(gEmpId);
//        Long userId = (Long) request.getSession().getAttribute(gUId);
//
//        // 未登录拦截
//        if (empId == null && userId == null) {
//            response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
//            return;
//        }

//        BaseContext.setCurrentId(empId != null ? empId : userId);
//
//        filterChain.doFilter(servletRequest, servletResponse);
//    }
}

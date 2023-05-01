package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Employee;
import com.itheima.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {
    @Autowired
    private EmployeeService employeeService;

    @Value("${global_name.employee_id}")
    private String gEmpId;

    @Value("${global_name.user_id}")
    private String gUId;

    // 添加员工
    @PostMapping
    public R<String> save(HttpServletRequest request, @RequestBody Employee employee) {
        System.out.println(employee);

        // 设置初始密码123456
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));

        // employee.setCreateTime(LocalDateTime.now()); // 当前系统时间
        // employee.setUpdateTime(LocalDateTime.now());

        // 获得当前登录用户的id
        // Long empId = (Long) request.getSession().getAttribute(Global.EMPLOYEE_ID);
        // employee.setCreateUser(empId);
        // employee.setUpdateUser(empId);

        System.out.println(employee);

        employeeService.save(employee);
        return R.success("新增员工成功");
    }

    // 查询员工数据
    @GetMapping("/page")
    public R<Page<Employee>> getPage(int page, int pageSize, String name) {
        log.info("page ===> " + page + "\npageSize ===> " + pageSize + "\nname ===> " + name);

        // 构造分页构造器
        Page<Employee> pageInfo = new Page<>(page, pageSize);
        // 构造条件构造器
        LambdaQueryWrapper<Employee> lqw = new LambdaQueryWrapper<>();
        // 添加一个过滤条件 org.apache.commons.lang.StringUtils 此工具容易导错包
        lqw.like(StringUtils.isNotEmpty(name), Employee::getName, name);
        // 添加排序条件
        lqw.orderByDesc(Employee::getUpdateTime);

        // 执行查询
        employeeService.page(pageInfo, lqw);
        return R.success(pageInfo);
    }

    // 根据id查询员工
    @GetMapping("{id}")
    public R<Employee> getById(@PathVariable long id) {
        Employee employee = employeeService.getById(id);
        return employee != null ? R.success(employee) : R.error("没有查询到对应员工信息");
    }

    // 更新
    @PutMapping
    public R<String> update(HttpServletRequest request, @RequestBody Employee employee) {
        Long empId = (Long) request.getSession().getAttribute(gEmpId);

        // employee.setUpdateUser(empId);
        // employee.setUpdateTime(LocalDateTime.now());

        System.out.println("线程id:" + Thread.currentThread().getId());

        employeeService.updateById(employee);
        return R.success("员工信息修改成功");
    }

    // 登录
    @PostMapping("/login")
    public R login(HttpServletRequest request, @RequestBody Employee employee) {
        /**
         * 1. 将页面提交的密码password进行md5加密处理
         * 2. 根据页面提交的用户名username查询数据库
         * 3. 如果没有查询到则返回登录失败结果
         * 4. 密码比对，如果不一致则返回登录失败结果
         * 5. 查看员工状态，如果为已禁用状态，则返回员工已禁用结果
         * 6. 登录成功，将员工id存入Session并返回登录成功结果
         * */

        // 1.
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());

        // 2.
        LambdaQueryWrapper<Employee> lqw = new LambdaQueryWrapper<>();
        lqw.eq(Employee::getUsername, employee.getUsername());
        Employee emp = employeeService.getOne(lqw);

        // 3.
        if (emp == null) return R.error("登录失败");

        if (!password.equals(emp.getPassword())) return R.error("登录失败");

        // 4. 0 禁用 1可用
        if (emp.getStatus() == 0) return R.error("账号已禁用");

        // 用户id删除 防止进入用户页面
        // request.getSession().removeAttribute(gUId);

        request.getSession().setAttribute(gEmpId, emp.getId());
        return R.success(emp);
    }

    // 退出登录
    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request) {
        // 清理Session中保存的当前登录员工的id
        request.getSession().removeAttribute(gEmpId);
        return R.success("退出成功");
    }
}

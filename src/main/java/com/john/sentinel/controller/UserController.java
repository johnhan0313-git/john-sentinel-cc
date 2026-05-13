package com.john.sentinel.controller;

import com.john.sentinel.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户控制器
 *
 * 演示场景：
 * 1. 匀速排队限流 - 用户注册接口
 * 2. 热点参数限流 - 用户信息查询
 *
 * @author john
 */
@Slf4j
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 用户注册
     *
     * 流控规则：QPS 限流 + 匀速排队，每秒最多 10 个请求，最大排队等待 5 秒
     * 测试命令：ab -n 100 -c 20 -p register.json -T application/json http://localhost:8088/api/user/register
     */
    @PostMapping("/register")
    public Map<String, Object> registerUser(@RequestParam String username,
                                            @RequestParam String password,
                                            @RequestParam String email,
                                            @RequestParam(required = false) String phone) {
        log.info("接收用户注册请求: username={}, email={}", username, email);

        Long userId = userService.registerUser(username, password, email, phone);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("userId", userId);
        result.put("message", "注册成功");
        return result;
    }

    /**
     * 查询用户信息
     *
     * 热点参数限流：针对特定用户 ID 进行限流
     */
    @GetMapping("/info/{userId}")
    public Map<String, Object> getUserInfo(@PathVariable Long userId) {
        log.info("接收查询用户信息请求: userId={}", userId);

        Map<String, Object> userInfo = userService.getUserInfo(userId);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", userInfo);
        return result;
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/update/{userId}")
    public Map<String, Object> updateUser(@PathVariable Long userId,
                                          @RequestBody Map<String, Object> updateData) {
        log.info("接收更新用户信息请求: userId=, updateData=", userId, updateData);

        boolean success = userService.updateUser(userId, updateData);

        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("message", success ? "更新成功" : "更新失败");
        return result;
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Map<String, Object> login(@RequestParam String username,
                                     @RequestParam String password) {
        log.info("接收用户登录请求: username={}", username);

        String token = userService.login(username, password);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("token", token);
        result.put("message", "登录成功");
        return result;
    }
}

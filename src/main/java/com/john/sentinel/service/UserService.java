package com.john.sentinel.service;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 用户服务
 *
 * 演示场景：
 * 1. 匀速排队限流
 * 2. 热点参数限流
 *
 * @author john
 */
@Slf4j
@Service
public class UserService {

    /**
     * 用户注册
     *
     * 流控规则：QPS 限流 + 匀速排队，每秒最多 10 个请求，最大排队等待 5 秒
     */
    @SentinelResource(
            value = "registerUser",
            blockHandler = "registerUserBlockHandler",
            fallback = "registerUserFallback"
    )
    public Long registerUser(String username, String password, String email, String phone) {
        log.info("开始注册用户: username={}, email={}", username, email);

        // 检查用户名是否已存在
        if (checkUsernameExists(username)) {
            throw new RuntimeException("用户名已存在");
        }

        // 检查邮箱是否已存在
        if (checkEmailExists(email)) {
            throw new RuntimeException("邮箱已被注册");
        }

        // 生成用户 ID
        Long userId = ThreadLocalRandom.current().nextLong(10000, 99999);

        // 保存用户信息
        saveUser(userId, username, password, email, phone);

        // 发送欢迎邮件（异步）
        sendWelcomeEmail(email);

        log.info("用户注册成功: userId={}, username={}", userId, username);
        return userId;
    }

    /**
     * 注册流控降级方法
     */
    public Long registerUserBlockHandler(String username, String password, String email, String phone, BlockException ex) {
        log.warn("用户注册被限流: username={}", username);
        throw new RuntimeException("注册人数过多，请稍后重试");
    }

    /**
     * 注册异常降级方法
     */
    public Long registerUserFallback(String username, String password, String email, String phone, Throwable ex) {
        log.error("用户注册异常降级: username={}, error={}", username, ex.getMessage());
        throw new RuntimeException("注册失败: " + ex.getMessage());
    }

    /**
     * 查询用户信息（热点参数限流）
     */
    @SentinelResource(
            value = "getUserInfo",
            blockHandler = "getUserInfoBlockHandler"
    )
    public Map<String, Object> getUserInfo(Long userId) {
        log.info("查询用户信息: userId={}", userId);

        // 从数据库查询用户信息
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("userId", userId);
        userInfo.put("username", "user_" + userId);
        userInfo.put("email", "user" + userId + "@example.com");
        userInfo.put("phone", "138****" + String.format("%04d", userId % 10000));
        userInfo.put("status", "ACTIVE");
        userInfo.put("createTime", System.currentTimeMillis());

        return userInfo;
    }

    /**
     * 查询用户信息流控降级方法
     */
    public Map<String, Object> getUserInfoBlockHandler(Long userId, BlockException ex) {
        log.warn("查询用户信息被限流: userId={}", userId);

        // 返回缓存数据或默认数据
        Map<String, Object> result = new HashMap<>();
        result.put("userId", userId);
        result.put("username", "user_" + userId);
        result.put("message", "查询过于频繁，返回缓存数据");
        return result;
    }

    /**
     * 更新用户信息
     */
    @SentinelResource(value = "updateUser")
    public boolean updateUser(Long userId, Map<String, Object> updateData) {
        log.info("更新用户信息: userId={}, updateData={}", userId, updateData);

        // 模拟更新用户信息
        return true;
    }

    /**
     * 用户登录
     */
    @SentinelResource(
            value = "login",
            blockHandler = "loginBlockHandler",
            fallback = "loginFallback"
    )
    public String login(String username, String password) {
        log.info("用户登录: username={}", username);

        // 验证用户名密码
        if (!validateCredentials(username, password)) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 生成 Token
        String token = UUID.randomUUID().toString().replace("-", "");

        log.info("用户登录成功: username={}, token={}", username, token);
        return token;
    }

    /**
     * 登录流控降级方法
     */
    public String loginBlockHandler(String username, String password, BlockException ex) {
        log.warn("用户登录被限流: username={}", username);
        throw new RuntimeException("登录请求过于频繁，请稍后重试");
    }

    /**
     * 登录异常降级方法
     */
    public String loginFallback(String username, String password, Throwable ex) {
        log.error("用户登录异常降级: username={}, error={}", username, ex.getMessage());
        throw new RuntimeException("登录失败: " + ex.getMessage());
    }

    // ========== 私有辅助方法 ==========

    private boolean checkUsernameExists(String username) {
        // 模拟检查用户名是否存在
        return ThreadLocalRandom.current().nextInt(100) < 5; // 5% 概率已存在
    }

    private boolean checkEmailExists(String email) {
        // 模拟检查邮箱是否存在
        return ThreadLocalRandom.current().nextInt(100) < 3; // 3% 概率已存在
    }

    private void saveUser(Long userId, String username, String password, String email, String phone) {
        log.debug("保存用户信息: userId={}, username={}", userId, username);
        // 实际应该保存到数据库
    }

    private void sendWelcomeEmail(String email) {
        log.debug("发送欢迎邮件: email={}", email);
        // 实际应该发送邮件
    }

    private boolean validateCredentials(String username, String password) {
        // 模拟验证用户名密码
        return ThreadLocalRandom.current().nextInt(100) > 10; // 90% 成功率
    }
}

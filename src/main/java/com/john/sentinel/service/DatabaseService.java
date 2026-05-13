package com.john.sentinel.service;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * 数据库服务
 *
 * 演示场景：
 * 1. 数据库查询慢调用比例熔断
 * 2. 数据库连接池保护
 *
 * @author john
 */
@Slf4j
@Service
public class DatabaseService {

    /**
     * 查询数据库
     *
     * 熔断规则：1 秒内请求数 >= 5 且慢调用比例 > 50% 时触发熔断
     */
    @SentinelResource(
            value = "queryDatabase",
            blockHandler = "queryDatabaseBlockHandler",
            fallback = "queryDatabaseFallback"
    )
    public Map<String, Object> queryDatabase(String sql) {
        log.info("执行数据库查询: sql={}", sql);

        // 模拟数据库查询延迟（30% 概率慢查询）
        try {
            if (ThreadLocalRandom.current().nextInt(100) < 30) {
                // 慢查询：超过 500ms
                TimeUnit.MILLISECONDS.sleep(ThreadLocalRandom.current().nextInt(500, 1000));
            } else {
                // 正常查询
                TimeUnit.MILLISECONDS.sleep(ThreadLocalRandom.current().nextInt(50, 200));
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 模拟查询失败（5% 概率）
        if (ThreadLocalRandom.current().nextInt(100) < 5) {
            throw new RuntimeException("数据库查询异常");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", "query result");
        return result;
    }

    /**
     * 数据库查询流控降级方法
     */
    public Map<String, Object> queryDatabaseBlockHandler(String sql, BlockException ex) {
        log.warn("数据库查询被限流/熔断: sql={}", sql);

        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", "数据库繁忙，请稍后重试");
        return result;
    }

    /**
     * 数据库查询异常降级方法
     */
    public Map<String, Object> queryDatabaseFallback(String sql, Throwable ex) {
        log.error("数据库查询异常降级: sql={}, error={}", sql, ex.getMessage());

        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", "数据库查询失败");
        return result;
    }

    /**
     * 保存订单
     */
    public void saveOrder(String orderId, Long userId, Long productId, Integer quantity) {
        log.info("保存订单: orderId={}, userId={}, productId={}, quantity={}", orderId, userId, productId, quantity);
        // 模拟保存订单到数据库
    }

    /**
     * 查询订单
     */
    public Map<String, Object> queryOrder(String orderId) {
        log.info("查询订单: orderId={}", orderId);

        // 模拟从数据库查询订单
        Map<String, Object> order = new HashMap<>();
        order.put("orderId", orderId);
        order.put("userId", 1001L);
        order.put("productId", 2001L);
        order.put("quantity", 1);
        order.put("status", "PENDING");
        order.put("createTime", System.currentTimeMillis());
        return order;
    }

    /**
     * 根据用户 ID 查询订单列表
     */
    public List<Map<String, Object>> queryOrdersByUser(Long userId, Integer page, Integer size) {
        log.info("查询用户订单列表: userId={}, page={}, size={}", userId, page, size);

        // 模拟分页查询
        List<Map<String, Object>> orders = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            Map<String, Object> order = new HashMap<>();
            order.put("orderId", "ORD" + System.currentTimeMillis() + i);
            order.put("userId", userId);
            order.put("productId", 2000L + i);
            order.put("quantity", 1);
            order.put("status", "PENDING");
            order.put("createTime", System.currentTimeMillis() - i * 1000);
            orders.add(order);
        }
        return orders;
    }

    /**
     * 统计用户订单数量
     */
    public int countOrdersByUser(Long userId) {
        log.info("统计用户订单数量: userId={}", userId);
        // 模拟统计
        return ThreadLocalRandom.current().nextInt(10, 100);
    }

    /**
     * 更新订单状态
     */
    public void updateOrderStatus(String orderId, String status) {
        log.info("更新订单状态: orderId={}, status={}", orderId, status);
        // 模拟更新订单状态
    }
}

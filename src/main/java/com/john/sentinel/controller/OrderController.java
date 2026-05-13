package com.john.sentinel.controller;

import com.john.sentinel.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 订单控制器
 *
 * 演示场景：
 * 1. QPS 流控 - 创建订单接口
 * 2. Warm Up 预热流控 - 查询订单接口
 * 3. 热点参数限流 - 根据用户 ID 限流
 *
 * @author john
 */
@Slf4j
@RestController
@RequestMapping("/api/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * 创建订单
     *
     * 流控规则：QPS 限流，每秒最多 100 个请求
     * 测试命令：ab -n 1000 -c 50 http://localhost:8088/api/order/create?userId=1001&productId=2001
     */
    @PostMapping("/create")
    public Map<String, Object> createOrder(@RequestParam Long userId,
                                           @RequestParam Long productId,
                                           @RequestParam(defaultValue = "1") Integer quantity) {
        log.info("接收创建订单请求: userId={}, productId={}, quantity={}", userId, productId, quantity);

        String orderId = orderService.createOrder(userId, productId, quantity);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("orderId", orderId);
        result.put("message", "订单创建成功");
        return result;
    }

    /**
     * 查询订单
     *
     * 流控规则：QPS 限流 + Warm Up 预热，每秒最多 200 个请求，预热时长 10 秒
     * 测试命令：ab -n 2000 -c 100 http://localhost:8088/api/order/query/ORD123456
     */
    @GetMapping("/query/{orderId}")
    public Map<String, Object> queryOrder(@PathVariable String orderId) {
        log.info("接收查询订单请求: orderId={}", orderId);

        Map<String, Object> orderInfo = orderService.queryOrder(orderId);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", orderInfo);
        return result;
    }

    /**
     * 根据用户 ID 查询订单列表
     *
     * 热点参数限流：针对特定用户 ID 进行限流
     * 测试命令：ab -n 1000 -c 50 http://localhost:8088/api/order/list?userId=1001
     */
    @GetMapping("/list")
    public Map<String, Object> queryOrdersByUser(@RequestParam Long userId,
                                                  @RequestParam(defaultValue = "1") Integer page,
                                                  @RequestParam(defaultValue = "10") Integer size) {
        log.info("接收查询用户订单列表请求: userId={}, page={}, size={}", userId, page, size);

        Map<String, Object> orderList = orderService.queryOrdersByUser(userId, page, size);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", orderList);
        return result;
    }

    /**
     * 取消订单
     */
    @PostMapping("/cancel/{orderId}")
    public Map<String, Object> cancelOrder(@PathVariable String orderId) {
        log.info("接收取消订单请求: orderId={}", orderId);

        boolean success = orderService.cancelOrder(orderId);

        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("message", success ? "订单取消成功" : "订单取消失败");
        return result;
    }
}

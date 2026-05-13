package com.john.sentinel.service;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 订单服务
 *
 * 演示 @SentinelResource 注解的使用：
 * 1. value: 资源名称
 * 2. blockHandler: 流控/熔断降级处理方法
 * 3. fallback: 异常降级处理方法
 *
 * @author john
 */
@Slf4j
@Service
public class OrderService {

    @Autowired
    private DatabaseService databaseService;

    @Autowired
    private ThirdPartyService thirdPartyService;

    /**
     * 创建订单
     *
     * @SentinelResource 注解说明：
     * - value: 资源名称，与流控规则中的 resource 对应
     * - blockHandler: 流控/熔断时的降级方法
     * - fallback: 业务异常时的降级方法
     */
    @SentinelResource(
            value = "createOrder",
            blockHandler = "createOrderBlockHandler",
            fallback = "createOrderFallback"
    )
    public String createOrder(Long userId, Long productId, Integer quantity) {
        log.info("开始创建订单: userId={}, productId={}, quantity={}", userId, productId, quantity);

        // 模拟业务逻辑
        // 1. 检查库存
        boolean hasStock = checkStock(productId, quantity);
        if (!hasStock) {
            throw new RuntimeException("库存不足");
        }

        // 2. 扣减库存
        deductStock(productId, quantity);

        // 3. 创建订单记录
        String orderId = "ORD" + System.currentTimeMillis() + ThreadLocalRandom.current().nextInt(1000);
        databaseService.saveOrder(orderId, userId, productId, quantity);

        // 4. 发送订单创建事件（异步）
        sendOrderCreatedEvent(orderId);

        log.info("订单创建成功: orderId={}", orderId);
        return orderId;
    }

    /**
     * 创建订单流控降级方法
     */
    public String createOrderBlockHandler(Long userId, Long productId, Integer quantity, BlockException ex) {
        log.warn("创建订单被限流/熔断: userId={}, productId={}, exception={}", userId, productId, ex.getClass().getSimpleName());
        throw new RuntimeException("系统繁忙，请稍后重试");
    }

    /**
     * 创建订单异常降级方法
     */
    public String createOrderFallback(Long userId, Long productId, Integer quantity, Throwable ex) {
        log.error("创建订单异常降级: userId={}, productId={}, error={}", userId, productId, ex.getMessage());
        throw new RuntimeException("订单创建失败: " + ex.getMessage());
    }

    /**
     * 查询订单
     */
    @SentinelResource(
            value = "queryOrder",
            blockHandler = "queryOrderBlockHandler"
    )
    public Map<String, Object> queryOrder(String orderId) {
        log.info("查询订单: orderId={}", orderId);

        // 从数据库查询订单信息
        Map<String, Object> orderInfo = databaseService.queryOrder(orderId);

        if (orderInfo == null) {
            throw new RuntimeException("订单不存在");
        }

        return orderInfo;
    }

    /**
     * 查询订单流控降级方法
     */
    public Map<String, Object> queryOrderBlockHandler(String orderId, BlockException ex) {
        log.warn("查询订单被限流: orderId={}", orderId);

        // 返回缓存数据或默认数据
        Map<String, Object> result = new HashMap<>();
        result.put("orderId", orderId);
        result.put("status", "UNKNOWN");
        result.put("message", "系统繁忙，请稍后重试");
        return result;
    }

    /**
     * 根据用户 ID 查询订单列表（热点参数限流）
     */
    @SentinelResource(
            value = "queryOrdersByUser",
            blockHandler = "queryOrdersByUserBlockHandler"
    )
    public Map<String, Object> queryOrdersByUser(Long userId, Integer page, Integer size) {
        log.info("查询用户订单列表: userId={}, page={}, size={}", userId, page, size);

        // 从数据库分页查询
        List<Map<String, Object>> orders = databaseService.queryOrdersByUser(userId, page, size);
        int total = databaseService.countOrdersByUser(userId);

        Map<String, Object> result = new HashMap<>();
        result.put("list", orders);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        return result;
    }

    /**
     * 查询用户订单列表流控降级方法
     */
    public Map<String, Object> queryOrdersByUserBlockHandler(Long userId, Integer page, Integer size, BlockException ex) {
        log.warn("查询用户订单列表被限流: userId={}", userId);

        Map<String, Object> result = new HashMap<>();
        result.put("list", Collections.emptyList());
        result.put("total", 0);
        result.put("message", "查询过于频繁，请稍后重试");
        return result;
    }

    /**
     * 取消订单
     */
    @SentinelResource(value = "cancelOrder")
    public boolean cancelOrder(String orderId) {
        log.info("取消订单: orderId={}", orderId);

        // 查询订单状态
        Map<String, Object> orderInfo = databaseService.queryOrder(orderId);
        if (orderInfo == null) {
            throw new RuntimeException("订单不存在");
        }

        String status = (String) orderInfo.get("status");
        if ("PAID".equals(status) || "SHIPPED".equals(status)) {
            throw new RuntimeException("订单已支付或已发货，无法取消");
        }

        // 更新订单状态
        databaseService.updateOrderStatus(orderId, "CANCELLED");

        // 恢复库存
        Long productId = (Long) orderInfo.get("productId");
        Integer quantity = (Integer) orderInfo.get("quantity");
        restoreStock(productId, quantity);

        return true;
    }

    // ========== 私有辅助方法 ==========

    private boolean checkStock(Long productId, Integer quantity) {
        // 模拟检查库存
        return ThreadLocalRandom.current().nextInt(100) > 5; // 95% 有库存
    }

    private void deductStock(Long productId, Integer quantity) {
        log.debug("扣减库存: productId={}, quantity={}", productId, quantity);
        // 实际应该调用库存服务
    }

    private void restoreStock(Long productId, Integer quantity) {
        log.debug("恢复库存: productId={}, quantity={}", productId, quantity);
        // 实际应该调用库存服务
    }

    private void sendOrderCreatedEvent(String orderId) {
        log.debug("发送订单创建事件: orderId={}", orderId);
        // 实际应该发送到消息队列
    }
}

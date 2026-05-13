package com.john.sentinel.service;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * 支付服务
 *
 * 演示场景：
 * 1. 并发线程数限流
 * 2. 异常数熔断
 * 3. 降级处理
 *
 * @author john
 */
@Slf4j
@Service
public class PaymentService {

    /**
     * 处理支付
     *
     * 流控规则：并发线程数限流，最多 50 个并发线程
     * 熔断规则：1 分钟内异常数 > 10 时触发熔断
     */
    @SentinelResource(
            value = "processPayment",
            blockHandler = "processPaymentBlockHandler",
            fallback = "processPaymentFallback"
    )
    public String processPayment(String orderId, BigDecimal amount, String paymentMethod) {
        log.info("开始处理支付: orderId={}, amount={}, paymentMethod={}", orderId, amount, paymentMethod);

        // 模拟支付处理耗时
        try {
            TimeUnit.MILLISECONDS.sleep(ThreadLocalRandom.current().nextInt(100, 500));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 模拟支付失败（10% 概率）
        if (ThreadLocalRandom.current().nextInt(100) < 10) {
            throw new RuntimeException("支付网关异常");
        }

        // 生成交易流水号
        String transactionId = "TXN" + System.currentTimeMillis() + ThreadLocalRandom.current().nextInt(1000);

        // 调用第三方支付接口（模拟）
        boolean paymentSuccess = callPaymentGateway(orderId, amount, paymentMethod);
        if (!paymentSuccess) {
            throw new RuntimeException("支付失败");
        }

        log.info("支付处理成功: transactionId={}", transactionId);
        return transactionId;
    }

    /**
     * 支付流控降级方法
     */
    public String processPaymentBlockHandler(String orderId, BigDecimal amount, String paymentMethod, BlockException ex) {
        log.warn("支付被限流/熔断: orderId={}, exception={}", orderId, ex.getClass().getSimpleName());
        throw new RuntimeException("支付系统繁忙，请稍后重试");
    }

    /**
     * 支付异常降级方法
     */
    public String processPaymentFallback(String orderId, BigDecimal amount, String paymentMethod, Throwable ex) {
        log.error("支付异常降级: orderId={}, error={}", orderId, ex.getMessage());

        // 记录失败日志，后续人工处理
        recordPaymentFailure(orderId, amount, paymentMethod, ex.getMessage());

        throw new RuntimeException("支付失败，请稍后重试");
    }

    /**
     * 查询支付状态
     */
    @SentinelResource(value = "queryPaymentStatus")
    public String queryPaymentStatus(String transactionId) {
        log.info("查询支付状态: transactionId={}", transactionId);

        // 模拟查询支付状态
        String[] statuses = {"SUCCESS", "PROCESSING", "FAILED"};
        return statuses[ThreadLocalRandom.current().nextInt(statuses.length)];
    }

    /**
     * 退款
     */
    @SentinelResource(
            value = "refund",
            blockHandler = "refundBlockHandler",
            fallback = "refundFallback"
    )
    public String refund(String transactionId, BigDecimal amount, String reason) {
        log.info("开始处理退款: transactionId={}, amount={}, reason={}", transactionId, amount, reason);

        // 模拟退款处理
        try {
            TimeUnit.MILLISECONDS.sleep(ThreadLocalRandom.current().nextInt(200, 800));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 生成退款单号
        String refundId = "RFD" + System.currentTimeMillis() + ThreadLocalRandom.current().nextInt(1000);

        log.info("退款处理成功: refundId={}", refundId);
        return refundId;
    }

    /**
     * 退款流控降级方法
     */
    public String refundBlockHandler(String transactionId, BigDecimal amount, String reason, BlockException ex) {
        log.warn("退款被限流: transactionId={}", transactionId);
        throw new RuntimeException("退款系统繁忙，请稍后重试");
    }

    /**
     * 退款异常降级方法
     */
    public String refundFallback(String transactionId, BigDecimal amount, String reason, Throwable ex) {
        log.error("退款异常降级: transactionId={}, error={}", transactionId, ex.getMessage());
        throw new RuntimeException("退款失败: " + ex.getMessage());
    }

    // ========== 私有辅助方法 ==========

    private boolean callPaymentGateway(String orderId, BigDecimal amount, String paymentMethod) {
        // 模拟调用第三方支付网关
        log.debug("调用支付网关: orderId={}, amount={}, paymentMethod={}", orderId, amount, paymentMethod);
        return ThreadLocalRandom.current().nextInt(100) > 5; // 95% 成功率
    }

    private void recordPaymentFailure(String orderId, BigDecimal amount, String paymentMethod, String errorMessage) {
        // 记录支付失败日志，实际应该写入数据库或日志系统
        log.error("记录支付失败: orderId={}, amount={}, paymentMethod={}, error={}",
                orderId, amount, paymentMethod, errorMessage);
    }
}

package com.john.sentinel.controller;

import com.john.sentinel.service.PaymentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 支付控制器
 *
 * 演示场景：
 * 1. 并发线程数限流 - 支付处理接口
 * 2. 异常数熔断 - 支付失败自动熔断
 * 3. 降级处理 - 支付服务降级
 *
 * @author john
 */
@Slf4j
@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    /**
     * 处理支付
     *
     * 流控规则：并发线程数限流，最多 50 个并发线程
     * 熔断规则：1 分钟内异常数 > 10 时触发熔断，熔断时长 30 秒
     * 测试命令：ab -n 500 -c 100 http://localhost:8088/api/payment/process
     */
    @PostMapping("/process")
    public Map<String, Object> processPayment(@RequestParam String orderId,
                                               @RequestParam BigDecimal amount,
                                               @RequestParam(defaultValue = "ALIPAY") String paymentMethod) {
        log.info("接收支付请求: orderId={}, amount={}, paymentMethod={}", orderId, amount, paymentMethod);

        String transactionId = paymentService.processPayment(orderId, amount, paymentMethod);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("transactionId", transactionId);
        result.put("message", "支付成功");
        return result;
    }

    /**
     * 查询支付状态
     */
    @GetMapping("/status/{transactionId}")
    public Map<String, Object> queryPaymentStatus(@PathVariable String transactionId) {
        log.info("接收查询支付状态请求: transactionId={}", transactionId);

        String status = paymentService.queryPaymentStatus(transactionId);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("transactionId", transactionId);
        result.put("status", status);
        return result;
    }

    /**
     * 退款
     */
    @PostMapping("/refund")
    public Map<String, Object> refund(@RequestParam String transactionId,
                                      @RequestParam BigDecimal amount,
                                      @RequestParam(required = false) String reason) {
        log.info("接收退款请求: transactionId={}, amount={}, reason={}", transactionId, amount, reason);

        String refundId = paymentService.refund(transactionId, amount, reason);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("refundId", refundId);
        result.put("message", "退款成功");
        return result;
    }

    /**
     * 模拟支付异常（用于测试熔断）
     */
    @PostMapping("/simulate-error")
    public Map<String, Object> simulateError(@RequestParam(defaultValue = "false") boolean throwError) {
        log.info("模拟支付异常: throwError={}", throwError);

        if (throwError) {
            throw new RuntimeException("模拟支付异常");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "正常处理");
        return result;
    }
}

package com.john.sentinel.service;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * 第三方服务调用
 *
 * 演示场景：
 * 1. 第三方 API 调用限流
 * 2. 异常比例熔断
 * 3. 降级处理
 *
 * @author john
 */
@Slf4j
@Service
public class ThirdPartyService {

    /**
     * 调用第三方 API
     *
     * 流控规则：QPS 限流，每秒最多 20 个请求
     * 熔断规则：1 秒内请求数 >= 5 且异常比例 > 30% 时触发熔断
     */
    @SentinelResource(
            value = "callThirdPartyApi",
            blockHandler = "callThirdPartyApiBlockHandler",
            fallback = "callThirdPartyApiFallback"
    )
    public String callThirdPartyApi(String apiName, String params) {
        log.info("调用第三方 API: apiName={}, params={}", apiName, params);

        // 模拟网络延迟
        try {
            TimeUnit.MILLISECONDS.sleep(ThreadLocalRandom.current().nextInt(50, 300));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 模拟第三方 API 异常（20% 概率）
        if (ThreadLocalRandom.current().nextInt(100) < 20) {
            throw new RuntimeException("第三方 API 调用失败: 网络超时");
        }

        // 返回模拟数据
        String result = "{\"code\":200,\"data\":\"success\",\"apiName\":\"" + apiName + "\"}";
        log.info("第三方 API 调用成功: apiName={}", apiName);
        return result;
    }

    /**
     * 第三方 API 流控降级方法
     */
    public String callThirdPartyApiBlockHandler(String apiName, String params, BlockException ex) {
        log.warn("第三方 API 被限流/熔断: apiName={}, exception={}", apiName, ex.getClass().getSimpleName());

        // 返回降级数据
        return "{\"code\":503,\"message\":\"服务繁忙，请稍后重试\"}";
    }

    /**
     * 第三方 API 异常降级方法
     */
    public String callThirdPartyApiFallback(String apiName, String params, Throwable ex) {
        log.error("第三方 API 异常降级: apiName={}, error={}", apiName, ex.getMessage());

        // 返回降级数据或缓存数据
        return "{\"code\":500,\"message\":\"第三方服务异常\",\"error\":\"" + ex.getMessage() + "\"}";
    }

    /**
     * 发送短信验证码
     */
    @SentinelResource(
            value = "sendSmsCode",
            blockHandler = "sendSmsCodeBlockHandler",
            fallback = "sendSmsCodeFallback"
    )
    public boolean sendSmsCode(String phone, String code) {
        log.info("发送短信验证码: phone={}, code={}", phone, code);

        // 模拟发送短信
        try {
            TimeUnit.MILLISECONDS.sleep(ThreadLocalRandom.current().nextInt(100, 500));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 模拟发送失败（10% 概率）
        if (ThreadLocalRandom.current().nextInt(100) < 10) {
            throw new RuntimeException("短信发送失败");
        }

        log.info("短信发送成功: phone={}", phone);
        return true;
    }

    /**
     * 发送短信流控降级方法
     */
    public boolean sendSmsCodeBlockHandler(String phone, String code, BlockException ex) {
        log.warn("发送短信被限流: phone={}", phone);
        throw new RuntimeException("短信发送过于频繁，请稍后重试");
    }

    /**
     * 发送短信异常降级方法
     */
    public boolean sendSmsCodeFallback(String phone, String code, Throwable ex) {
        log.error("发送短信异常降级: phone={}, error={}", phone, ex.getMessage());
        throw new RuntimeException("短信发送失败: " + ex.getMessage());
    }

    /**
     * 查询物流信息
     */
    @SentinelResource(
            value = "queryLogistics",
            blockHandler = "queryLogisticsBlockHandler",
            fallback = "queryLogisticsFallback"
    )
    public String queryLogistics(String trackingNumber) {
        log.info("查询物流信息: trackingNumber={}", trackingNumber);

        // 模拟查询物流
        try {
            TimeUnit.MILLISECONDS.sleep(ThreadLocalRandom.current().nextInt(200, 800));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 模拟查询失败（15% 概率）
        if (ThreadLocalRandom.current().nextInt(100) < 15) {
            throw new RuntimeException("物流信息查询失败");
        }

        return "{\"trackingNumber\":\"" + trackingNumber + "\",\"status\":\"运输中\",\"location\":\"北京市\"}";
    }

    /**
     * 查询物流流控降级方法
     */
    public String queryLogisticsBlockHandler(String trackingNumber, BlockException ex) {
        log.warn("查询物流被限流: trackingNumber={}", trackingNumber);
        return "{\"trackingNumber\":\"" + trackingNumber + "\",\"message\":\"查询过于频繁，请稍后重试\"}";
    }

    /**
     * 查询物流异常降级方法
     */
    public String queryLogisticsFallback(String trackingNumber, Throwable ex) {
        log.error("查询物流异常降级: trackingNumber={}, error={}", trackingNumber, ex.getMessage());
        return "{\"trackingNumber\":\"" + trackingNumber + "\",\"message\":\"物流信息暂时无法查询\"}";
    }
}

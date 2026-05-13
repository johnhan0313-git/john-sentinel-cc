package com.john.sentinel.aspect;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * Sentinel 监控切面
 *
 * 用于监控方法执行情况，记录限流/熔断事件
 *
 * @author john
 */
@Slf4j
@Aspect
@Component
public class SentinelAspect {

    /**
     * 监控所有 Controller 方法
     */
    @Around("execution(* com.john.sentinel.controller..*.*(..))")
    public Object monitorController(ProceedingJoinPoint joinPoint) throws Throwable {
        String resourceName = joinPoint.getSignature().toShortString();
        long startTime = System.currentTimeMillis();

        Entry entry = null;
        try {
            // 使用 Sentinel 保护资源
            entry = SphU.entry(resourceName);

            // 执行目标方法
            Object result = joinPoint.proceed();

            long duration = System.currentTimeMillis() - startTime;
            log.info("请求成功: resource={}, duration={}ms", resourceName, duration);

            return result;

        } catch (BlockException e) {
            // 被限流/熔断
            log.warn("请求被限流/熔断: resource={}, exception={}", resourceName, e.getClass().getSimpleName());
            throw e;

        } catch (Throwable throwable) {
            // 业务异常
            log.error("请求异常: resource={}, error={}", resourceName, throwable.getMessage());
            throw throwable;

        } finally {
            if (entry != null) {
                entry.exit();
            }
        }
    }
}

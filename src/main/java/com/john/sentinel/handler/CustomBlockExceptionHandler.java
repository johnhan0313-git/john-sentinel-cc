package com.john.sentinel.handler;

import com.alibaba.csp.sentinel.adapter.spring.webmvc.callback.BlockExceptionHandler;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeException;
import com.alibaba.csp.sentinel.slots.block.flow.FlowException;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowException;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * 自定义 Sentinel 限流异常处理器
 *
 * 统一处理 Sentinel 的各种限流/熔断异常，返回友好的错误信息
 *
 * @author john
 */
@Slf4j
@Component
public class CustomBlockExceptionHandler implements BlockExceptionHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, BlockException e) throws Exception {
        log.warn("请求被 Sentinel 拦截: uri={}, exception={}", request.getRequestURI(), e.getClass().getSimpleName());

        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("timestamp", System.currentTimeMillis());
        result.put("path", request.getRequestURI());

        // 根据不同的异常类型返回不同的错误信息
        if (e instanceof FlowException) {
            // 流控异常
            response.setStatus(429); // Too Many Requests
            result.put("code", 429);
            result.put("message", "请求过于频繁，请稍后重试");
            result.put("type", "FLOW_LIMIT");

        } else if (e instanceof DegradeException) {
            // 熔断降级异常
            response.setStatus(503); // Service Unavailable
            result.put("code", 503);
            result.put("message", "服务暂时不可用，请稍后重试");
            result.put("type", "DEGRADE");

        } else if (e instanceof ParamFlowException) {
            // 热点参数限流异常
            response.setStatus(429);
            result.put("code", 429);
            result.put("message", "访问过于频繁，请稍后重试");
            result.put("type", "PARAM_FLOW_LIMIT");

        } else if (e instanceof AuthorityException) {
            // 授权规则异常
            response.setStatus(403); // Forbidden
            result.put("code", 403);
            result.put("message", "无权限访问");
            result.put("type", "AUTHORITY");

        } else {
            // 其他异常
            response.setStatus(500);
            result.put("code", 500);
            result.put("message", "系统繁忙，请稍后重试");
            result.put("type", "SYSTEM_BLOCK");
        }

        // 返回 JSON 格式的错误信息
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(JSON.toJSONString(result));
    }
}

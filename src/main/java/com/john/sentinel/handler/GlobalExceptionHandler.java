package com.john.sentinel.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * 全局异常处理器
 *
 * 统一处理应用中的各种异常
 *
 * @author john
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常
     */
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, Object> handleRuntimeException(RuntimeException e) {
        log.error("业务异常: {}", e.getMessage(), e);

        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("code", 500);
        result.put("message", e.getMessage());
        result.put("timestamp", System.currentTimeMillis());
        return result;
    }

    /**
     * 处理非法参数异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleIllegalArgumentException(IllegalArgumentException e) {
        log.error("参数异常: {}", e.getMessage(), e);

        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("code", 400);
        result.put("message", "参数错误: " + e.getMessage());
        result.put("timestamp", System.currentTimeMillis());
        return result;
    }

    /**
     * 处理所有未捕获的异常
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, Object> handleException(Exception e) {
        log.error("系统异常: {}", e.getMessage(), e);

        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("code", 500);
        result.put("message", "系统异常，请联系管理员");
        result.put("timestamp", System.currentTimeMillis());
        return result;
    }
}

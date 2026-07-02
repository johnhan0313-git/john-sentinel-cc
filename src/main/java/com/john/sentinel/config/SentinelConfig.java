package com.john.sentinel.config;

import com.alibaba.csp.sentinel.annotation.aspectj.SentinelResourceAspect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Sentinel 核心配置：仅注册注解切面。
 * <p>
 * 规则加载策略见 {@link SentinelLocalRuleConfiguration}（local）或
 * {@code spring.cloud.sentinel.datasource}（nacos）。
 */
@Configuration
public class SentinelConfig {

    @Bean
    public SentinelResourceAspect sentinelResourceAspect() {
        return new SentinelResourceAspect();
    }
}

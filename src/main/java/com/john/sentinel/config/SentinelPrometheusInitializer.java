package com.john.sentinel.config;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * 在 Sentinel InitFunc（含 Prometheus Exporter）执行前注入 JVM 参数。
 */
public class SentinelPrometheusInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        ConfigurableEnvironment env = applicationContext.getEnvironment();
        if (!Boolean.parseBoolean(env.getProperty("sentinel.prometheus.enabled", "false"))) {
            return;
        }

        System.setProperty("csp.sentinel.prometheus.fetch.port",
                env.getProperty("sentinel.prometheus.port", "9092"));
        System.setProperty("csp.sentinel.prometheus.app",
                env.getProperty("spring.application.name", "unknown-app"));
        System.setProperty("csp.sentinel.prometheus.fetch.types",
                env.getProperty("sentinel.prometheus.fetch-types",
                        "passQps|blockQps|exceptionQps|rt|concurrency"));
    }
}

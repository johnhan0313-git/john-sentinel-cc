package com.john.sentinel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Sentinel 示例应用启动类
 *
 * 启动参数说明：
 * -Dcsp.sentinel.dashboard.server=localhost:8080  # Sentinel Dashboard 地址
 * -Dcsp.sentinel.api.port=8719                    # Sentinel 客户端端口
 * -Dproject.name=john-sentinel-cc                 # 应用名称
 *
 * @author john
 */
@SpringBootApplication
public class SentinelApplication {

    public static void main(String[] args) {
        // 设置 Sentinel 系统属性（也可以通过 JVM 参数设置）
        System.setProperty("csp.sentinel.dashboard.server", "localhost:8080");
        System.setProperty("project.name", "john-sentinel-cc");

        SpringApplication.run(SentinelApplication.class, args);

        System.out.println("\n========================================");
        System.out.println("Sentinel Application Started Successfully!");
        System.out.println("Sentinel Dashboard: http://localhost:8080");
        System.out.println("Application Port: 8088");
        System.out.println("========================================\n");
    }
}

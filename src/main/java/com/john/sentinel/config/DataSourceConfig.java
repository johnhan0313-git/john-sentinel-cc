package com.john.sentinel.config;

import com.alibaba.csp.sentinel.datasource.FileRefreshableDataSource;
import com.alibaba.csp.sentinel.datasource.ReadableDataSource;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.csp.sentinel.slots.system.SystemRule;
import com.alibaba.csp.sentinel.slots.system.SystemRuleManager;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.List;

/**
 * Sentinel 数据源配置
 *
 * 支持多种数据源：
 * 1. 文件数据源（本地文件持久化）
 * 2. Nacos 数据源（动态配置中心）
 * 3. Apollo 数据源（动态配置中心）
 * 4. ZooKeeper 数据源
 *
 * 本示例使用文件数据源，生产环境建议使用 Nacos/Apollo
 *
 * @author john
 */
@Slf4j
@Configuration
public class DataSourceConfig {

    private static final String RULE_DIR = "src/main/resources/sentinel-rules/";

    /**
     * 初始化文件数据源
     *
     * 文件数据源特点：
     * 1. 规则持久化到本地文件
     * 2. 支持热更新（文件变化自动加载）
     * 3. 适合开发测试环境
     *
     * 生产环境建议使用 Nacos 数据源：
     * <pre>
     * ReadableDataSource<String, List<FlowRule>> flowRuleDataSource = new NacosDataSource<>(
     *     "localhost:8848",
     *     "DEFAULT_GROUP",
     *     "sentinel-flow-rules",
     *     source -> JSON.parseObject(source, new TypeReference<List<FlowRule>>() {})
     * );
     * FlowRuleManager.register2Property(flowRuleDataSource.getProperty());
     * </pre>
     */
    @PostConstruct
    public void initDataSource() {
        try {
            // 初始化流控规则数据源
            initFlowRuleDataSource();

            // 初始化熔断降级规则数据源
            initDegradeRuleDataSource();

            // 初始化系统保护规则数据源
            initSystemRuleDataSource();

            log.info("✓ Sentinel 数据源初始化完成");
        } catch (Exception e) {
            log.error("Sentinel 数据源初始化失败", e);
        }
    }

    /**
     * 初始化流控规则数据源
     */
    private void initFlowRuleDataSource() throws Exception {
        String flowRulePath = RULE_DIR + "flow-rules.json";
        File flowRuleFile = new File(flowRulePath);

        if (!flowRuleFile.exists()) {
            log.warn("流控规则文件不存在: {}", flowRulePath);
            return;
        }

        ReadableDataSource<String, List<FlowRule>> flowRuleDataSource = new FileRefreshableDataSource<>(
                flowRulePath,
                source -> JSON.parseObject(source, new TypeReference<List<FlowRule>>() {})
        );

        FlowRuleManager.register2Property(flowRuleDataSource.getProperty());
        log.info("✓ 流控规则数据源初始化完成: {}", flowRulePath);
    }

    /**
     * 初始化熔断降级规则数据源
     */
    private void initDegradeRuleDataSource() throws Exception {
        String degradeRulePath = RULE_DIR + "degrade-rules.json";
        File degradeRuleFile = new File(degradeRulePath);

        if (!degradeRuleFile.exists()) {
            log.warn("熔断降级规则文件不存在: {}", degradeRulePath);
            return;
        }

        ReadableDataSource<String, List<DegradeRule>> degradeRuleDataSource = new FileRefreshableDataSource<>(
                degradeRulePath,
                source -> JSON.parseObject(source, new TypeReference<List<DegradeRule>>() {})
        );

        DegradeRuleManager.register2Property(degradeRuleDataSource.getProperty());
        log.info("✓ 熔断降级规则数据源初始化完成: {}", degradeRulePath);
    }

    /**
     * 初始化系统保护规则数据源
     */
    private void initSystemRuleDataSource() throws Exception {
        String systemRulePath = RULE_DIR + "system-rules.json";
        File systemRuleFile = new File(systemRulePath);

        if (!systemRuleFile.exists()) {
            log.warn("系统保护规则文件不存在: {}", systemRulePath);
            return;
        }

        ReadableDataSource<String, List<SystemRule>> systemRuleDataSource = new FileRefreshableDataSource<>(
                systemRulePath,
                source -> JSON.parseObject(source, new TypeReference<List<SystemRule>>() {})
        );

        SystemRuleManager.register2Property(systemRuleDataSource.getProperty());
        log.info("✓ 系统保护规则数据源初始化完成: {}", systemRulePath);
    }
}

package com.john.sentinel.init;

import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRuleManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Sentinel 规则初始化器
 *
 * 在应用启动时初始化热点参数限流规则
 *
 * @author john
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "sentinel.rules.source", havingValue = "local", matchIfMissing = true)
public class SentinelRuleInitializer implements CommandLineRunner {

    @Override
    public void run(String... args) {
        initParamFlowRules();
        log.info("✓ Sentinel 规则初始化器执行完成");
    }

    /**
     * 初始化热点参数限流规则
     *
     * 热点参数限流场景：
     * 1. 针对特定用户 ID 限流（防止单个用户刷接口）
     * 2. 针对特定商品 ID 限流（防止热点商品击穿缓存）
     * 3. 针对特定 IP 限流（防止恶意攻击）
     */
    private void initParamFlowRules() {
        List<ParamFlowRule> rules = new ArrayList<>();

        // 规则1: 查询用户订单列表 - 针对 userId 参数限流
        // 每个用户每秒最多查询 10 次
        ParamFlowRule userOrderRule = new ParamFlowRule("queryOrdersByUser");
        userOrderRule.setParamIdx(0); // 第一个参数（userId）
        userOrderRule.setCount(10); // 每秒最多 10 次
        userOrderRule.setGrade(1); // QPS 模式
        userOrderRule.setDurationInSec(1); // 统计窗口 1 秒
        rules.add(userOrderRule);

        // 规则2: 查询用户信息 - 针对 userId 参数限流
        // 每个用户每秒最多查询 20 次
        ParamFlowRule userInfoRule = new ParamFlowRule("getUserInfo");
        userInfoRule.setParamIdx(0); // 第一个参数（userId）
        userInfoRule.setCount(20); // 每秒最多 20 次
        userInfoRule.setGrade(1); // QPS 模式
        userInfoRule.setDurationInSec(1);
        rules.add(userInfoRule);

        // 规则3: 创建订单 - 针对 userId 参数限流
        // 每个用户每秒最多创建 5 个订单
        ParamFlowRule createOrderRule = new ParamFlowRule("createOrder");
        createOrderRule.setParamIdx(0); // 第一个参数（userId）
        createOrderRule.setCount(5); // 每秒最多 5 次
        createOrderRule.setGrade(1); // QPS 模式
        createOrderRule.setDurationInSec(1);
        rules.add(createOrderRule);

        ParamFlowRuleManager.loadRules(rules);
        log.info("✓ 热点参数限流规则初始化完成，共 {} 条规则", rules.size());
    }
}

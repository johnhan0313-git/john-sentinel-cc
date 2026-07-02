package com.john.sentinel.config;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.csp.sentinel.slots.system.SystemRule;
import com.alibaba.csp.sentinel.slots.system.SystemRuleManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

/**
 * 本地规则初始化（开发/测试默认模式）。
 * <p>
 * 当 {@code sentinel.rules.source=nacos} 时禁用，改由 Nacos 数据源推送规则。
 */
@Slf4j
@Configuration
@ConditionalOnProperty(name = "sentinel.rules.source", havingValue = "local", matchIfMissing = true)
public class SentinelLocalRuleConfiguration {

    @PostConstruct
    public void initRules() {
        initFlowRules();
        initDegradeRules();
        initSystemRules();
        log.info("✓ 本地 Sentinel 规则初始化完成（sentinel.rules.source=local）");
    }

    private void initFlowRules() {
        List<FlowRule> rules = new ArrayList<>();

        FlowRule orderRule = new FlowRule();
        orderRule.setResource("createOrder");
        orderRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        orderRule.setCount(100);
        orderRule.setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_DEFAULT);
        rules.add(orderRule);

        FlowRule queryOrderRule = new FlowRule();
        queryOrderRule.setResource("queryOrder");
        queryOrderRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        queryOrderRule.setCount(200);
        queryOrderRule.setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_WARM_UP);
        queryOrderRule.setWarmUpPeriodSec(10);
        rules.add(queryOrderRule);

        FlowRule paymentRule = new FlowRule();
        paymentRule.setResource("processPayment");
        paymentRule.setGrade(RuleConstant.FLOW_GRADE_THREAD);
        paymentRule.setCount(50);
        rules.add(paymentRule);

        FlowRule registerRule = new FlowRule();
        registerRule.setResource("registerUser");
        registerRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        registerRule.setCount(10);
        registerRule.setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_RATE_LIMITER);
        registerRule.setMaxQueueingTimeMs(5000);
        rules.add(registerRule);

        FlowRule thirdPartyRule = new FlowRule();
        thirdPartyRule.setResource("callThirdPartyApi");
        thirdPartyRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        thirdPartyRule.setCount(20);
        rules.add(thirdPartyRule);

        FlowRuleManager.loadRules(rules);
        log.info("✓ 流控规则初始化完成，共 {} 条", rules.size());
    }

    private void initDegradeRules() {
        List<DegradeRule> rules = new ArrayList<>();

        DegradeRule dbSlowRule = new DegradeRule();
        dbSlowRule.setResource("queryDatabase");
        dbSlowRule.setGrade(RuleConstant.DEGRADE_GRADE_RT);
        dbSlowRule.setCount(500);
        dbSlowRule.setTimeWindow(10);
        dbSlowRule.setMinRequestAmount(5);
        dbSlowRule.setSlowRatioThreshold(0.5);
        dbSlowRule.setStatIntervalMs(1000);
        rules.add(dbSlowRule);

        DegradeRule thirdPartyExceptionRule = new DegradeRule();
        thirdPartyExceptionRule.setResource("callThirdPartyApi");
        thirdPartyExceptionRule.setGrade(RuleConstant.DEGRADE_GRADE_EXCEPTION_RATIO);
        thirdPartyExceptionRule.setCount(0.3);
        thirdPartyExceptionRule.setTimeWindow(15);
        thirdPartyExceptionRule.setMinRequestAmount(5);
        thirdPartyExceptionRule.setStatIntervalMs(1000);
        rules.add(thirdPartyExceptionRule);

        DegradeRule paymentExceptionCountRule = new DegradeRule();
        paymentExceptionCountRule.setResource("processPayment");
        paymentExceptionCountRule.setGrade(RuleConstant.DEGRADE_GRADE_EXCEPTION_COUNT);
        paymentExceptionCountRule.setCount(10);
        paymentExceptionCountRule.setTimeWindow(30);
        paymentExceptionCountRule.setMinRequestAmount(5);
        paymentExceptionCountRule.setStatIntervalMs(60000);
        rules.add(paymentExceptionCountRule);

        DegradeRuleManager.loadRules(rules);
        log.info("✓ 熔断降级规则初始化完成，共 {} 条", rules.size());
    }

    private void initSystemRules() {
        List<SystemRule> rules = new ArrayList<>();

        SystemRule cpuRule = new SystemRule();
        cpuRule.setHighestCpuUsage(0.8);
        rules.add(cpuRule);

        SystemRule rtRule = new SystemRule();
        rtRule.setAvgRt(1000);
        rules.add(rtRule);

        SystemRule threadRule = new SystemRule();
        threadRule.setMaxThread(200);
        rules.add(threadRule);

        SystemRule qpsRule = new SystemRule();
        qpsRule.setQps(500);
        rules.add(qpsRule);

        SystemRuleManager.loadRules(rules);
        log.info("✓ 系统保护规则初始化完成，共 {} 条", rules.size());
    }
}

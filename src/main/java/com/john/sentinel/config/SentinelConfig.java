package com.john.sentinel.config;

import com.alibaba.csp.sentinel.annotation.aspectj.SentinelResourceAspect;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.csp.sentinel.slots.system.SystemRule;
import com.alibaba.csp.sentinel.slots.system.SystemRuleManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

/**
 * Sentinel 核心配置类
 *
 * 配置内容：
 * 1. 启用 @SentinelResource 注解支持
 * 2. 初始化流控规则
 * 3. 初始化熔断降级规则
 * 4. 初始化系统保护规则
 *
 * @author john
 */
@Configuration
public class SentinelConfig {

    /**
     * 注册 Sentinel 注解切面
     */
    @Bean
    public SentinelResourceAspect sentinelResourceAspect() {
        return new SentinelResourceAspect();
    }

    /**
     * 初始化 Sentinel 规则
     * 生产环境建议使用 Nacos/Apollo 等配置中心动态管理规则
     */
    @PostConstruct
    public void initRules() {
        initFlowRules();
        initDegradeRules();
        initSystemRules();
    }

    /**
     * 初始化流控规则
     *
     * 场景覆盖：
     * 1. QPS 限流 - 订单接口
     * 2. 并发线程数限流 - 支付接口
     * 3. 关联限流 - 读写接口关联
     * 4. 链路限流 - 特定调用链路
     */
    private void initFlowRules() {
        List<FlowRule> rules = new ArrayList<>();

        // 规则1: 订单创建接口 QPS 限流（每秒最多 100 个请求）
        FlowRule orderRule = new FlowRule();
        orderRule.setResource("createOrder");
        orderRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        orderRule.setCount(100);
        orderRule.setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_DEFAULT); // 直接拒绝
        rules.add(orderRule);

        // 规则2: 订单查询接口 QPS 限流（每秒最多 200 个请求，使用 Warm Up 预热）
        FlowRule queryOrderRule = new FlowRule();
        queryOrderRule.setResource("queryOrder");
        queryOrderRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        queryOrderRule.setCount(200);
        queryOrderRule.setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_WARM_UP); // 预热模式
        queryOrderRule.setWarmUpPeriodSec(10); // 预热时长 10 秒
        rules.add(queryOrderRule);

        // 规则3: 支付接口并发线程数限流（最多 50 个并发线程）
        FlowRule paymentRule = new FlowRule();
        paymentRule.setResource("processPayment");
        paymentRule.setGrade(RuleConstant.FLOW_GRADE_THREAD);
        paymentRule.setCount(50);
        rules.add(paymentRule);

        // 规则4: 用户注册接口 QPS 限流（每秒最多 10 个请求，使用排队等待）
        FlowRule registerRule = new FlowRule();
        registerRule.setResource("registerUser");
        registerRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        registerRule.setCount(10);
        registerRule.setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_RATE_LIMITER); // 匀速排队
        registerRule.setMaxQueueingTimeMs(5000); // 最大排队等待时间 5 秒
        rules.add(registerRule);

        // 规则5: 第三方 API 调用限流（每秒最多 20 个请求）
        FlowRule thirdPartyRule = new FlowRule();
        thirdPartyRule.setResource("callThirdPartyApi");
        thirdPartyRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        thirdPartyRule.setCount(20);
        rules.add(thirdPartyRule);

        FlowRuleManager.loadRules(rules);
        System.out.println("✓ 流控规则初始化完成，共 " + rules.size() + " 条规则");
    }

    /**
     * 初始化熔断降级规则
     *
     * 场景覆盖：
     * 1. 慢调用比例熔断 - 数据库查询
     * 2. 异常比例熔断 - 第三方服务调用
     * 3. 异常数熔断 - 关键业务接口
     */
    private void initDegradeRules() {
        List<DegradeRule> rules = new ArrayList<>();

        // 规则1: 数据库查询慢调用比例熔断
        // 当 1 秒内请求数 >= 5 且慢调用比例 > 50% 时触发熔断，熔断时长 10 秒
        DegradeRule dbSlowRule = new DegradeRule();
        dbSlowRule.setResource("queryDatabase");
        dbSlowRule.setGrade(RuleConstant.DEGRADE_GRADE_RT); // 慢调用比例
        dbSlowRule.setCount(500); // 响应时间超过 500ms 视为慢调用
        dbSlowRule.setTimeWindow(10); // 熔断时长 10 秒
        dbSlowRule.setMinRequestAmount(5); // 最小请求数
        dbSlowRule.setSlowRatioThreshold(0.5); // 慢调用比例阈值 50%
        dbSlowRule.setStatIntervalMs(1000); // 统计时长 1 秒
        rules.add(dbSlowRule);

        // 规则2: 第三方服务异常比例熔断
        // 当 1 秒内请求数 >= 5 且异常比例 > 30% 时触发熔断，熔断时长 15 秒
        DegradeRule thirdPartyExceptionRule = new DegradeRule();
        thirdPartyExceptionRule.setResource("callThirdPartyApi");
        thirdPartyExceptionRule.setGrade(RuleConstant.DEGRADE_GRADE_EXCEPTION_RATIO); // 异常比例
        thirdPartyExceptionRule.setCount(0.3); // 异常比例阈值 30%
        thirdPartyExceptionRule.setTimeWindow(15); // 熔断时长 15 秒
        thirdPartyExceptionRule.setMinRequestAmount(5);
        thirdPartyExceptionRule.setStatIntervalMs(1000);
        rules.add(thirdPartyExceptionRule);

        // 规则3: 支付接口异常数熔断
        // 当 1 分钟内异常数 > 10 时触发熔断，熔断时长 30 秒
        DegradeRule paymentExceptionCountRule = new DegradeRule();
        paymentExceptionCountRule.setResource("processPayment");
        paymentExceptionCountRule.setGrade(RuleConstant.DEGRADE_GRADE_EXCEPTION_COUNT); // 异常数
        paymentExceptionCountRule.setCount(10); // 异常数阈值
        paymentExceptionCountRule.setTimeWindow(30); // 熔断时长 30 秒
        paymentExceptionCountRule.setMinRequestAmount(5);
        paymentExceptionCountRule.setStatIntervalMs(60000); // 统计时长 1 分钟
        rules.add(paymentExceptionCountRule);

        DegradeRuleManager.loadRules(rules);
        System.out.println("✓ 熔断降级规则初始化完成，共 " + rules.size() + " 条规则");
    }

    /**
     * 初始化系统保护规则
     *
     * 系统自适应保护：
     * 1. Load 自适应（仅对 Linux/Unix 有效）
     * 2. CPU 使用率
     * 3. 平均 RT
     * 4. 并发线程数
     * 5. 入口 QPS
     */
    private void initSystemRules() {
        List<SystemRule> rules = new ArrayList<>();

        // 规则1: CPU 使用率保护（CPU 使用率超过 80% 时触发保护）
        SystemRule cpuRule = new SystemRule();
        cpuRule.setHighestCpuUsage(0.8);
        rules.add(cpuRule);

        // 规则2: 系统平均 RT 保护（平均响应时间超过 1000ms 时触发保护）
        SystemRule rtRule = new SystemRule();
        rtRule.setAvgRt(1000);
        rules.add(rtRule);

        // 规则3: 并发线程数保护（系统并发线程数超过 200 时触发保护）
        SystemRule threadRule = new SystemRule();
        threadRule.setMaxThread(200);
        rules.add(threadRule);

        // 规则4: 入口 QPS 保护（系统入口 QPS 超过 500 时触发保护）
        SystemRule qpsRule = new SystemRule();
        qpsRule.setQps(500);
        rules.add(qpsRule);

        SystemRuleManager.loadRules(rules);
        System.out.println("✓ 系统保护规则初始化完成，共 " + rules.size() + " 条规则");
    }
}

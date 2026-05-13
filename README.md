# Sentinel 流控降级实战项目

[![Java](https://img.shields.io/badge/Java-1.8-blue.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7.18-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Sentinel](https://img.shields.io/badge/Sentinel-1.8.7-orange.svg)](https://sentinelguard.io/)

## 项目简介

本项目是一个完整的 Sentinel 流控降级实战示例，展示了 Sentinel 在大厂级微服务架构中的常见应用场景和最佳实践。

## 核心特性

### 1. 流量控制（Flow Control）
- **QPS 限流**：订单创建接口每秒最多 100 个请求
- **并发线程数限流**：支付接口最多 50 个并发线程
- **Warm Up 预热**：订单查询接口预热 10 秒，避免冷启动
- **匀速排队**：用户注册接口排队等待，最大等待 5 秒

### 2. 熔断降级（Circuit Breaking）
- **慢调用比例熔断**：数据库查询响应时间超过 500ms 且比例 > 50% 时熔断
- **异常比例熔断**：第三方 API 调用异常比例 > 30% 时熔断
- **异常数熔断**：支付接口 1 分钟内异常数 > 10 时熔断

### 3. 热点参数限流（Hot Param Flow Control）
- 针对特定用户 ID 限流，防止单个用户刷接口
- 针对特定商品 ID 限流，防止热点商品击穿缓存

### 4. 系统自适应保护（System Adaptive Protection）
- CPU 使用率保护：CPU > 80% 时触发保护
- 平均响应时间保护：RT > 1000ms 时触发保护
- 并发线程数保护：线程数 > 200 时触发保护
- 入口 QPS 保护：QPS > 500 时触发保护

### 5. 动态规则配置
- 支持文件数据源（本地文件持久化）
- 支持 Nacos 数据源（动态配置中心）
- 规则热更新，无需重启应用

## 项目结构

```
john-sentinel-cc/
├── src/
│   ├── main/
│   │   ├── java/com/john/sentinel/
│   │   │   ├── SentinelApplication.java          # 启动类
│   │   │   ├── config/
│   │   │   │   ├── SentinelConfig.java           # Sentinel 核心配置
│   │   │   │   └── DataSourceConfig.java         # 数据源配置
│   │   │   ├── controller/
│   │   │   │   ├── OrderController.java          # 订单接口
│   │   │   │   ├── PaymentController.java        # 支付接口
│   │   │   │   └── UserController.java           # 用户接口
│   │   │   ├── service/
│   │   │   │   ├── OrderService.java             # 订单服务
│   │   │   │   ├── PaymentService.java           # 支付服务
│   │   │   │   ├── UserService.java              # 用户服务
│   │   │   │   ├── ThirdPartyService.java        # 第三方服务
│   │   │   │   └── DatabaseService.java          # 数据库服务
│   │   │   ├── handler/
│   │   │   │   ├── CustomBlockExceptionHandler.java  # 限流异常处理
│   │   │   │   └── GlobalExceptionHandler.java       # 全局异常处理
│   │   │   ├── aspect/
│   │   │   │   └── SentinelAspect.java           # 监控切面
│   │   │   └── init/
│   │   │       └── SentinelRuleInitializer.java  # 规则初始化器
│   │   └── resources/
│   │       ├── application.yml                    # 主配置文件
│   │       ├── application-dev.yml                # 开发环境配置
│   │       ├── application-prod.yml               # 生产环境配置
│   │       └── sentinel-rules/                    # 规则配置文件
│   │           ├── flow-rules.json                # 流控规则
│   │           ├── degrade-rules.json             # 熔断规则
│   │           └── system-rules.json              # 系统规则
│   └── test/
│       └── java/com/john/sentinel/
│           └── SentinelTest.java                  # 功能测试
└── pom.xml                                        # Maven 配置
```

## 快速开始

### 1. 环境要求

- JDK 1.8+
- Maven 3.6+
- Sentinel Dashboard 1.8.7

### 2. 启动 Sentinel Dashboard

下载 Sentinel Dashboard：

```bash
wget https://github.com/alibaba/Sentinel/releases/download/1.8.6/sentinel-dashboard-1.8.6.jar
```

启动 Dashboard：

```bash
java -Dserver.port=8080 -jar sentinel-dashboard-1.8.6.jar
```

访问 Dashboard：http://localhost:8080（默认用户名/密码：sentinel/sentinel）

### 3. 启动应用

```bash
# 使用快速启动脚本
./start.sh

# 或者使用 Maven 启动
mvn spring-boot:run

# 或者编译后启动
mvn clean compile
mvn spring-boot:run
```

应用启动后访问：http://localhost:8088

### 4. 查看监控

访问 Sentinel Dashboard：http://localhost:8080

在左侧菜单选择 `john-sentinel-cc` 应用，即可查看实时监控数据。

## 业务场景演示

**重要提示**：所有包含 `&` 符号的 URL 都必须用引号包裹，否则会出现 shell 解析错误。

### 场景 1：订单创建 QPS 限流

**规则**：每秒最多 100 个请求

**测试命令**：
```bash
# 使用 Apache Bench 压测（注意：URL 必须用引号包裹）
ab -n 1000 -c 50 "http://localhost:8088/api/order/create?userId=1001&productId=2001&quantity=1"

# 或者使用 curl 循环测试
for i in {1..100}; do
  curl -X POST "http://localhost:8088/api/order/create?userId=1001&productId=2001&quantity=1"
  echo ""
done
```

**预期结果**：
- 前 100 个请求成功
- 超出部分返回 429 状态码，提示"请求过于频繁，请稍后重试"

### 场景 2：支付接口并发线程数限流

**规则**：最多 50 个并发线程

**测试命令**：
```bash
# 注意：URL 必须用引号包裹
ab -n 500 -c 100 "http://localhost:8088/api/payment/process?orderId=ORD123456&amount=99.99&paymentMethod=ALIPAY"

# 或者使用 curl 测试
curl -X POST "http://localhost:8088/api/payment/process?orderId=ORD123456&amount=99.99&paymentMethod=ALIPAY"
```

**预期结果**：
- 最多 50 个线程同时处理
- 超出部分被限流

### 场景 3：用户注册匀速排队

**规则**：每秒最多 10 个请求，最大排队等待 5 秒

**测试命令**：
```bash
# 注意：URL 必须用引号包裹
ab -n 100 -c 20 "http://localhost:8088/api/user/register?username=test&password=123456&email=test@example.com"

# 或者使用 curl 测试
curl -X POST "http://localhost:8088/api/user/register?username=testuser&password=123456&email=test@example.com"
```
ab -n 100 -c 20 http://localhost:8088/api/user/register?username=test&password=123456&email=test@example.com
```

**预期结果**：
- 请求匀速通过，不会突发
- 排队超过 5 秒的请求被拒绝

### 场景 4：数据库慢调用熔断

**规则**：1 秒内请求数 >= 5 且慢调用比例 > 50% 时触发熔断

**测试方法**：
1. 多次调用订单查询接口
2. 当数据库响应变慢时，自动触发熔断
3. 熔断期间直接返回降级数据

### 场景 5：第三方 API 异常比例熔断

**规则**：1 秒内请求数 >= 5 且异常比例 > 30% 时触发熔断

**测试方法**：
1. 调用第三方 API 接口
2. 当异常比例超过阈值时，自动触发熔断
3. 熔断期间返回降级数据

## 核心注解说明

### @SentinelResource

```java
@SentinelResource(
    value = "createOrder",              // 资源名称
    blockHandler = "createOrderBlockHandler",  // 流控/熔断降级方法
    fallback = "createOrderFallback"    // 异常降级方法
)
public String createOrder(Long userId, Long productId, Integer quantity) {
    // 业务逻辑
}

// 流控降级方法
public String createOrderBlockHandler(Long userId, Long productId, Integer quantity, BlockException ex) {
    // 返回降级数据
}

// 异常降级方法
public String createOrderFallback(Long userId, Long productId, Integer quantity, Throwable ex) {
    // 返回降级数据
}
```

## 规则配置说明

### 流控规则（FlowRule）

```java
FlowRule rule = new FlowRule();
rule.setResource("createOrder");        // 资源名称
rule.setGrade(RuleConstant.FLOW_GRADE_QPS);  // QPS 模式
rule.setCount(100);                     // 阈值
rule.setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_DEFAULT);  // 直接拒绝
```

**控制行为**：
- `CONTROL_BEHAVIOR_DEFAULT`：直接拒绝
- `CONTROL_BEHAVIOR_WARM_UP`：预热模式
- `CONTROL_BEHAVIOR_RATE_LIMITER`：匀速排队

### 熔断规则（DegradeRule）

```java
DegradeRule rule = new DegradeRule();
rule.setResource("queryDatabase");      // 资源名称
rule.setGrade(RuleConstant.DEGRADE_GRADE_RT);  // 慢调用比例
rule.setCount(500);                     // 响应时间阈值（ms）
rule.setTimeWindow(10);                 // 熔断时长（秒）
rule.setSlowRatioThreshold(0.5);        // 慢调用比例阈值
```

**熔断策略**：
- `DEGRADE_GRADE_RT`：慢调用比例
- `DEGRADE_GRADE_EXCEPTION_RATIO`：异常比例
- `DEGRADE_GRADE_EXCEPTION_COUNT`：异常数

## 监控指标

### 实时监控
- QPS（每秒请求数）
- 响应时间（RT）
- 并发线程数
- 通过 QPS
- 拒绝 QPS
- 异常 QPS

### 规则管理
- 流控规则
- 熔断规则
- 热点规则
- 系统规则
- 授权规则

## 生产环境最佳实践

### 1. 使用配置中心

生产环境建议使用 Nacos/Apollo 等配置中心管理规则：

```java
// Nacos 数据源配置
ReadableDataSource<String, List<FlowRule>> flowRuleDataSource = new NacosDataSource<>(
    "nacos-server:8848",
    "DEFAULT_GROUP",
    "sentinel-flow-rules",
    source -> JSON.parseObject(source, new TypeReference<List<FlowRule>>() {})
);
FlowRuleManager.register2Property(flowRuleDataSource.getProperty());
```

### 2. 规则持久化

将规则持久化到配置中心，避免应用重启后规则丢失。

### 3. 集群流控

对于分布式系统，使用集群流控模式，统一管理集群的流量。

### 4. 监控告警

接入监控系统（Prometheus + Grafana），设置告警规则。

### 5. 降级策略

- 返回缓存数据
- 返回默认值
- 返回友好提示
- 记录日志，人工介入

## 常见问题

### 1. Dashboard 看不到应用

**原因**：
- 应用未启动
- Dashboard 地址配置错误
- 应用未触发过任何请求

**解决**：
- 检查配置：`spring.cloud.sentinel.transport.dashboard`
- 触发任意接口请求
- 检查防火墙和网络连接

### 2. 规则不生效

**原因**：
- 资源名称不匹配
- 规则配置错误
- 规则未加载

**解决**：
- 检查资源名称是否一致
- 检查规则配置是否正确
- 查看日志确认规则是否加载

### 3. 熔断后无法恢复

**原因**：
- 熔断时长设置过长
- 服务未恢复正常

**解决**：
- 调整熔断时长
- 修复服务问题
- 手动重置熔断状态

### 4. Shell 解析错误：`zsh: parse error near '&'`

**原因**：
- URL 中的 `&` 符号在 shell 中有特殊含义
- 未使用引号包裹 URL

**解决**：
```bash
# ❌ 错误写法
ab -n 1000 -c 50 http://localhost:8088/api/order/create?userId=1001&productId=2001

# ✅ 正确写法（使用双引号）
ab -n 1000 -c 50 "http://localhost:8088/api/order/create?userId=1001&productId=2001&quantity=1"

# ✅ 正确写法（使用单引号）
ab -n 1000 -c 50 'http://localhost:8088/api/order/create?userId=1001&productId=2001&quantity=1'
```

**规则**：所有包含特殊字符（`&`、`?`、`=` 等）的 URL 都应该用引号包裹。

## 参考资料

- [Sentinel 官方文档](https://sentinelguard.io/)
- [Sentinel GitHub](https://github.com/alibaba/Sentinel)
- [Spring Cloud Alibaba](https://spring-cloud-alibaba-group.github.io/)

## 作者

John - 大厂级流控降级实践

## 许可证

MIT License

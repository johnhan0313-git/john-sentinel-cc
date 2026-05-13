# Sentinel 项目构建说明

## 项目状态

✅ **项目已成功创建并编译通过**

## 项目结构

```
john-sentinel-cc/
├── pom.xml                                    # Maven 配置
├── README.md                                  # 详细文档
├── settings.xml                               # Maven 仓库配置
├── .gitignore                                 # Git 忽略文件
└── src/
    ├── main/
    │   ├── java/com/john/sentinel/
    │   │   ├── SentinelApplication.java       # 启动类 ✅
    │   │   ├── config/
    │   │   │   ├── SentinelConfig.java        # Sentinel 核心配置 ✅
    │   │   │   └── DataSourceConfig.java      # 数据源配置 ✅
    │   │   ├── controller/
    │   │   │   ├── OrderController.java       # 订单接口 ✅
    │   │   │   ├── PaymentController.java     # 支付接口 ✅
    │   │   │   └── UserController.java        # 用户接口 ✅
    │   │   ├── service/
    │   │   │   ├── OrderService.java          # 订单服务 ✅
    │   │   │   ├── PaymentService.java        # 支付服务 ✅
    │   │   │   ├── UserService.java           # 用户服务 ✅
    │   │   │   ├── ThirdPartyService.java     # 第三方服务 ✅
    │   │   │   └── DatabaseService.java       # 数据库服务 ✅
    │   │   ├── handler/
    │   │   │   ├── CustomBlockExceptionHandler.java  # 限流异常处理 ✅
    │   │   │   └── GlobalExceptionHandler.java       # 全局异常处理 ✅
    │   │   ├── aspect/
    │   │   │   └── SentinelAspect.java        # 监控切面 ✅
    │   │   └── init/
    │   │       └── SentinelRuleInitializer.java  # 规则初始化器 ✅
    │   └── resources/
    │       ├── application.yml                 # 主配置文件 ✅
    │       ├── application-dev.yml             # 开发环境配置 ✅
    │       ├── application-prod.yml            # 生产环境配置 ✅
    │       └── sentinel-rules/
    │           ├── flow-rules.json             # 流控规则 ✅
    │           ├── degrade-rules.json          # 熔断规则 ✅
    │           └── system-rules.json           # 系统规则 ✅
    └── test/
        └── java/com/john/sentinel/
            └── SentinelTest.java               # 功能测试 ✅
```

## 核心特性

### 1. 流量控制
- ✅ QPS 限流（订单创建：100 QPS）
- ✅ 并发线程数限流（支付处理：50 线程）
- ✅ Warm Up 预热（订单查询：200 QPS，预热 10 秒）
- ✅ 匀速排队（用户注册：10 QPS，排队 5 秒）

### 2. 熔断降级
- ✅ 慢调用比例熔断（数据库查询：RT > 500ms，比例 > 50%）
- ✅ 异常比例熔断（第三方 API：异常比例 > 30%）
- ✅ 异常数熔断（支付接口：1 分钟异常数 > 10）

### 3. 热点参数限流
- ✅ 用户维度限流（防止单用户刷接口）
- ✅ 商品维度限流（防止热点商品击穿缓存）

### 4. 系统自适应保护
- ✅ CPU 使用率保护（> 80%）
- ✅ 平均响应时间保护（> 1000ms）
- ✅ 并发线程数保护（> 200）
- ✅ 入口 QPS 保护（> 500）

### 5. 动态规则配置
- ✅ 文件数据源（本地持久化）
- ✅ 支持 Nacos 数据源（代码已实现，需配置）

## 编译状态

✅ **编译成功**

```bash
mvn clean compile
```

编译输出：
- 15 个 Java 源文件编译成功
- 所有 class 文件生成在 target/classes 目录

## 如何运行

### 方式 1：使用 Maven 运行

```bash
mvn spring-boot:run
```

### 方式 2：使用 IDE 运行

1. 导入项目到 IDEA/Eclipse
2. 运行 `SentinelApplication.java` 主类

### 方式 3：打包后运行

```bash
# 打包（如果遇到权限问题，可以忽略警告）
mvn clean package -DskipTests

# 运行
java -jar target/john-sentinel-cc-1.0.0-SNAPSHOT.jar
```

## 启动 Sentinel Dashboard

在运行应用之前，需要先启动 Sentinel Dashboard：

```bash
# 下载 Dashboard
wget https://github.com/alibaba/Sentinel/releases/download/1.8.6/sentinel-dashboard-1.8.6.jar

# 启动 Dashboard
java -Dserver.port=8080 -jar sentinel-dashboard-1.8.6.jar
```

访问：http://localhost:8080（用户名/密码：sentinel/sentinel）

## 测试接口

应用启动后（默认端口 8088），可以测试以下接口：

### 订单接口
```bash
# 创建订单
curl -X POST "http://localhost:8088/api/order/create?userId=1001&productId=2001&quantity=1"

# 查询订单
curl "http://localhost:8088/api/order/query/ORD123456"

# 查询用户订单列表
curl "http://localhost:8088/api/order/list?userId=1001"
```

### 支付接口
```bash
# 处理支付
curl -X POST "http://localhost:8088/api/payment/process?orderId=ORD123456&amount=99.99&paymentMethod=ALIPAY"

# 查询支付状态
curl "http://localhost:8088/api/payment/status/TXN123456"
```

### 用户接口
```bash
# 用户注册
curl -X POST "http://localhost:8088/api/user/register?username=testuser&password=123456&email=test@example.com"

# 查询用户信息
curl "http://localhost:8088/api/user/info/1001"

# 用户登录
curl -X POST "http://localhost:8088/api/user/login?username=testuser&password=123456"
```

## 压力测试

使用 Apache Bench 进行压力测试：

```bash
# 测试订单创建接口（QPS 限流）
ab -n 1000 -c 50 "http://localhost:8088/api/order/create?userId=1001&productId=2001&quantity=1"

# 测试支付接口（并发线程数限流）
ab -n 500 -c 100 "http://localhost:8088/api/payment/process?orderId=ORD123456&amount=99.99&paymentMethod=ALIPAY"

# 测试用户注册接口（匀速排队）
ab -n 100 -c 20 "http://localhost:8088/api/user/register?username=test&password=123456&email=test@example.com"
```

## 监控查看

1. 启动应用后，访问 Sentinel Dashboard：http://localhost:8080
2. 在左侧菜单选择 `john-sentinel-cc` 应用
3. 查看实时监控数据：
   - 实时监控：QPS、RT、并发线程数
   - 簇点链路：资源调用链路
   - 流控规则：查看和修改流控规则
   - 熔断规则：查看和修改熔断规则

## 已知问题

1. **Maven 本地仓库权限警告**：编译时会出现 `.lastUpdated` 文件写入权限警告，但不影响编译结果，可以忽略。

2. **网络问题**：如果无法访问阿里云 Maven 仓库，项目已配置使用本地已有的依赖。

## 技术栈

- Java 1.8
- Spring Boot 2.7.18
- Sentinel 1.8.6
- Spring Cloud Alibaba 2021.0.5.0
- Lombok
- FastJSON
- Guava

## 下一步

1. ✅ 项目已创建并编译成功
2. 📝 启动 Sentinel Dashboard
3. 📝 运行应用
4. 📝 测试接口
5. 📝 查看监控数据
6. 📝 压力测试验证限流效果

## 参考文档

详细使用说明请查看 [README.md](README.md)

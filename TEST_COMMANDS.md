# Sentinel 测试命令速查表

## 重要提示

⚠️ **所有包含 `&` 符号的 URL 都必须用引号包裹！**

```bash
# ❌ 错误
ab -n 1000 -c 50 http://localhost:8088/api/order/create?userId=1001&productId=2001

# ✅ 正确
ab -n 1000 -c 50 "http://localhost:8088/api/order/create?userId=1001&productId=2001&quantity=1"
```

## 快速测试命令

### 1. 订单接口测试

```bash
# 创建订单（QPS 限流：100/秒）
ab -n 1000 -c 50 "http://localhost:8088/api/order/create?userId=1001&productId=2001&quantity=1"

# 查询订单（Warm Up 预热：200/秒）
ab -n 2000 -c 100 "http://localhost:8088/api/order/query/ORD123456"

# 查询用户订单列表（热点参数限流）
ab -n 1000 -c 50 "http://localhost:8088/api/order/list?userId=1001&page=1&size=10"

# 取消订单
curl -X POST "http://localhost:8088/api/order/cancel/ORD123456"
```

### 2. 支付接口测试

```bash
# 处理支付（并发线程数限流：50 线程）
ab -n 500 -c 100 "http://localhost:8088/api/payment/process?orderId=ORD123456&amount=99.99&paymentMethod=ALIPAY"

# 查询支付状态
curl "http://localhost:8088/api/payment/status/TXN123456"

# 退款
curl -X POST "http://localhost:8088/api/payment/refund?transactionId=TXN123456&amount=99.99&reason=test"

# 模拟支付异常（测试熔断）
curl -X POST "http://localhost:8088/api/payment/simulate-error?throwError=true"
```

### 3. 用户接口测试

```bash
# 用户注册（匀速排队：10/秒，排队 5 秒）
ab -n 100 -c 20 "http://localhost:8088/api/user/register?username=test&password=123456&email=test@example.com"

# 查询用户信息（热点参数限流）
ab -n 1000 -c 50 "http://localhost:8088/api/user/info/1001"

# 用户登录
curl -X POST "http://localhost:8088/api/user/login?username=testuser&password=123456"

# 更新用户信息
curl -X PUT "http://localhost:8088/api/user/update/1001" \
  -H "Content-Type: application/json" \
  -d '{"nickname":"John","phone":"13800138000"}'
```

## 使用 curl 循环测试

```bash
# 循环创建订单（测试 QPS 限流）
for i in {1..100}; do
  curl -X POST "http://localhost:8088/api/order/create?userId=1001&productId=2001&quantity=1"
  echo ""
done

# 循环注册用户（测试匀速排队）
for i in {1..50}; do
  curl -X POST "http://localhost:8088/api/user/register?username=user$i&password=123456&email=user$i@example.com"
  echo ""
  sleep 0.05
done

# 循环查询用户信息（测试热点参数限流）
for i in {1..100}; do
  curl "http://localhost:8088/api/user/info/1001"
  echo ""
done
```

## Apache Bench 参数说明

```bash
ab [options] [http[s]://]hostname[:port]/path

常用参数：
-n requests     总请求数
-c concurrency  并发数
-t timelimit    测试时长（秒）
-p postfile     POST 数据文件
-T content-type Content-Type 头
-H header       自定义请求头
-k              启用 HTTP KeepAlive
```

## 测试场景对照表

| 接口 | 限流类型 | 阈值 | 测试命令 |
|------|---------|------|---------|
| 创建订单 | QPS | 100/秒 | `ab -n 1000 -c 50 "http://localhost:8088/api/order/create?userId=1001&productId=2001&quantity=1"` |
| 查询订单 | QPS + Warm Up | 200/秒 | `ab -n 2000 -c 100 "http://localhost:8088/api/order/query/ORD123456"` |
| 处理支付 | 并发线程数 | 50 线程 | `ab -n 500 -c 100 "http://localhost:8088/api/payment/process?orderId=ORD123456&amount=99.99&paymentMethod=ALIPAY"` |
| 用户注册 | QPS + 匀速排队 | 10/秒 | `ab -n 100 -c 20 "http://localhost:8088/api/user/register?username=test&password=123456&email=test@example.com"` |
| 查询用户信息 | 热点参数限流 | 20/秒/用户 | `ab -n 1000 -c 50 "http://localhost:8088/api/user/info/1001"` |

## 预期结果

### QPS 限流
- 请求速率超过阈值时返回 **429 Too Many Requests**
- 响应体：`{"success":false,"code":429,"message":"请求过于频繁，请稍后重试","type":"FLOW_LIMIT"}`

### 并发线程数限流
- 并发线程数超过阈值时返回 **429 Too Many Requests**
- 超出部分请求被拒绝

### 熔断降级
- 触发熔断后返回 **503 Service Unavailable**
- 响应体：`{"success":false,"code":503,"message":"服务暂时不可用，请稍后重试","type":"DEGRADE"}`

### 匀速排队
- 请求匀速通过，不会突发
- 排队超时返回 **429 Too Many Requests**

## 监控查看

1. 访问 Sentinel Dashboard：http://localhost:8080
2. 选择应用：`john-sentinel-cc`
3. 查看实时监控：
   - **实时监控**：QPS、RT、并发线程数
   - **簇点链路**：资源调用链路
   - **流控规则**：查看和修改流控规则
   - **熔断规则**：查看和修改熔断规则

## 故障排查

### 1. 应用未出现在 Dashboard
```bash
# 检查应用是否启动
curl http://localhost:8088/actuator/health

# 检查 Sentinel 配置
curl http://localhost:8088/actuator/sentinel
```

### 2. 限流不生效
- 检查资源名称是否匹配
- 查看 Dashboard 中的规则配置
- 检查应用日志

### 3. 熔断不生效
- 确保请求数达到最小阈值
- 检查统计时间窗口
- 查看异常/慢调用比例

## 快速启动

```bash
# 1. 启动 Sentinel Dashboard
java -Dserver.port=8080 -jar sentinel-dashboard-1.8.6.jar

# 2. 启动应用
./start.sh

# 3. 测试接口
curl "http://localhost:8088/api/order/query/ORD123456"

# 4. 查看监控
open http://localhost:8080
```

#!/bin/bash

echo "=========================================="
echo "Sentinel 项目快速启动脚本"
echo "=========================================="
echo ""

# 检查 Java 版本
echo "检查 Java 环境..."
java -version 2>&1 | head -1

# 编译项目
echo ""
echo "编译项目..."
mvn clean compile -DskipTests

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ 编译成功！"
    echo ""
    echo "=========================================="
    echo "启动应用..."
    echo "=========================================="
    echo ""
    echo "提示："
    echo "1. 请先启动 Sentinel Dashboard："
    echo "   java -Dserver.port=8080 -jar sentinel-dashboard-1.8.6.jar"
    echo ""
    echo "2. Dashboard 地址：http://localhost:8080"
    echo "   用户名/密码：sentinel/sentinel"
    echo ""
    echo "3. 应用端口：8088"
    echo ""
    echo "=========================================="
    echo ""

    # 启动应用
    mvn spring-boot:run
else
    echo ""
    echo "❌ 编译失败，请检查错误信息"
    exit 1
fi

#!/usr/bin/env bash
# 启动 Nacos + Prometheus + Grafana 观测栈
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "${SCRIPT_DIR}/../docker"

docker compose up -d

echo ""
echo "观测栈已启动："
echo "  Nacos:      http://localhost:8848/nacos  (nacos/nacos)"
echo "  Prometheus: http://localhost:9090"
echo "  Grafana:    http://localhost:3000       (admin/admin)"
echo ""
echo "下一步："
echo "  1. ./scripts/push-nacos-rules.sh"
echo "  2. mvn spring-boot:run -Dspring-boot.run.profiles=dev-nacos-observability"
echo "  3. 打流量后在 Grafana 用 PromQL 查询 Sentinel 指标"

#!/usr/bin/env bash
# 将 nacos-rules/ 下的规则 JSON 推送到 Nacos 配置中心
#
# 用法：
#   ./scripts/push-nacos-rules.sh
#   APP_NAME=john-sentinel-cc NACOS_ADDR=127.0.0.1:8848 ./scripts/push-nacos-rules.sh
#   ./scripts/push-nacos-rules.sh --gateway   # 同时推送网关规则

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"
RULES_DIR="${PROJECT_DIR}/src/main/resources/nacos-rules"

NACOS_ADDR="${NACOS_ADDR:-localhost:8848}"
NACOS_NAMESPACE="${NACOS_NAMESPACE:-}"
NACOS_GROUP="${NACOS_GROUP:-SENTINEL_GROUP}"
NACOS_USERNAME="${NACOS_USERNAME:-nacos}"
NACOS_PASSWORD="${NACOS_PASSWORD:-nacos}"
APP_NAME="${APP_NAME:-john-sentinel-cc}"

PUSH_GATEWAY=false
if [[ "${1:-}" == "--gateway" ]]; then
  PUSH_GATEWAY=true
fi

push_config() {
  local data_id="$1"
  local file_path="$2"

  if [[ ! -f "${file_path}" ]]; then
    echo "跳过：文件不存在 ${file_path}"
    return
  fi

  echo "推送 ${data_id} <- ${file_path}"
  curl -sf -X POST "http://${NACOS_ADDR}/nacos/v1/cs/configs" \
    -u "${NACOS_USERNAME}:${NACOS_PASSWORD}" \
    --data-urlencode "tenant=${NACOS_NAMESPACE}" \
    --data-urlencode "group=${NACOS_GROUP}" \
    --data-urlencode "dataId=${data_id}" \
    --data-urlencode "type=json" \
    --data-urlencode "content@${file_path}" \
    > /dev/null
  echo "  ✓ 完成"
}

echo "Nacos: ${NACOS_ADDR}, Group: ${NACOS_GROUP}, App: ${APP_NAME}"
echo ""

# Web 应用规则（5 种）
push_config "${APP_NAME}-flow-rules"        "${RULES_DIR}/flow-rules.json"
push_config "${APP_NAME}-degrade-rules"     "${RULES_DIR}/degrade-rules.json"
push_config "${APP_NAME}-system-rules"     "${RULES_DIR}/system-rules.json"
push_config "${APP_NAME}-param-flow-rules"  "${RULES_DIR}/param-flow-rules.json"
push_config "${APP_NAME}-authority-rules"  "${RULES_DIR}/authority-rules.json"

# Gateway 规则（可选）
if [[ "${PUSH_GATEWAY}" == "true" ]]; then
  push_config "${APP_NAME}-gw-flow-rules"   "${RULES_DIR}/gw-flow-rules.json"
  push_config "${APP_NAME}-gw-api-group"    "${RULES_DIR}/gw-api-group.json"
fi

echo ""
echo "全部规则推送完成。"
echo "启动应用: java -jar app.jar --spring.profiles.active=dev,nacos"

#!/usr/bin/env bash

set -Eeuo pipefail

APP_DIR="${APP_DIR:-/opt/cloud-224}"
APP_IMAGE="${APP_IMAGE:-ghcr.io/zeezeng/cloud-224}"
APP_IMAGE_TAG="${APP_IMAGE_TAG:-latest}"
APP_PORT="${APP_PORT:-8080}"
JAVA_OPTS="${JAVA_OPTS:--Xms512m -Xmx512m -Dfile.encoding=UTF-8}"
MARS_DEMO_MODE="${MARS_DEMO_MODE:-false}"
APP_LOG_LEVEL="${APP_LOG_LEVEL:-info}"

MYSQL_ROOT_PASSWORD="${MYSQL_ROOT_PASSWORD:-root}"
MYSQL_DATABASE="${MYSQL_DATABASE:-mars-system}"
MYSQL_PORT="${MYSQL_PORT:-3306}"

REDIS_PASSWORD="${REDIS_PASSWORD:-}"
REDIS_PORT="${REDIS_PORT:-6379}"
REDIS_DATABASE="${REDIS_DATABASE:-10}"

GHCR_USERNAME="${GHCR_USERNAME:-}"
GHCR_TOKEN="${GHCR_TOKEN:-}"

log() {
  printf '\n[%s] %s\n' "$(date '+%F %T')" "$*"
}

need_root_cmd() {
  if [[ "${EUID}" -eq 0 ]]; then
    "$@"
  elif command -v sudo >/dev/null 2>&1; then
    sudo "$@"
  else
    echo "需要 root 权限执行：$*" >&2
    exit 1
  fi
}

detect_pkg_manager() {
  if command -v apt-get >/dev/null 2>&1; then
    echo "apt"
  elif command -v dnf >/dev/null 2>&1; then
    echo "dnf"
  elif command -v yum >/dev/null 2>&1; then
    echo "yum"
  else
    echo ""
  fi
}

install_packages() {
  local manager
  manager="$(detect_pkg_manager)"
  [[ -n "$manager" ]] || {
    echo "未识别的包管理器，请先手动安装 docker / curl。" >&2
    exit 1
  }

  case "$manager" in
    apt)
      need_root_cmd apt-get update
      need_root_cmd apt-get install -y "$@"
      ;;
    dnf)
      need_root_cmd dnf install -y "$@"
      ;;
    yum)
      need_root_cmd yum install -y "$@"
      ;;
  esac
}

ensure_docker() {
  if command -v docker >/dev/null 2>&1 && docker compose version >/dev/null 2>&1; then
    return
  fi

  log "未检测到 Docker 或 Docker Compose，正在安装"
  if ! command -v curl >/dev/null 2>&1; then
    install_packages curl
  fi

  curl -fsSL https://get.docker.com | need_root_cmd sh
  if command -v systemctl >/dev/null 2>&1; then
    need_root_cmd systemctl enable --now docker
  fi
}

write_env() {
  mkdir -p "${APP_DIR}"

  if [[ ! -f "${APP_DIR}/.env" ]]; then
    cat > "${APP_DIR}/.env" <<EOF
APP_PORT=${APP_PORT}
APP_IMAGE=${APP_IMAGE}
APP_IMAGE_TAG=${APP_IMAGE_TAG}
JAVA_OPTS=${JAVA_OPTS}
MARS_DEMO_MODE=${MARS_DEMO_MODE}
APP_LOG_LEVEL=${APP_LOG_LEVEL}
MYSQL_ROOT_PASSWORD=${MYSQL_ROOT_PASSWORD}
MYSQL_DATABASE=${MYSQL_DATABASE}
MYSQL_PORT=${MYSQL_PORT}
REDIS_PASSWORD=${REDIS_PASSWORD}
REDIS_PORT=${REDIS_PORT}
REDIS_DATABASE=${REDIS_DATABASE}
EOF
    log "已生成 ${APP_DIR}/.env"
  else
    log "检测到 .env 已存在，保留现有配置"
  fi
}

write_compose() {
  cat > "${APP_DIR}/docker-compose.yml" <<'EOF'
# Mars Admin 生产编排
# 生产环境默认拉取 GitHub Actions 发布的镜像，不在服务器本地构建。

services:
  mysql:
    image: mysql:8.0
    container_name: mars-mysql
    restart: unless-stopped
    environment:
      MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD:-root}
      MYSQL_DATABASE: ${MYSQL_DATABASE:-mars-system}
      TZ: Asia/Shanghai
    command:
      - --character-set-server=utf8mb4
      - --collation-server=utf8mb4_unicode_ci
      - --default-authentication-plugin=mysql_native_password
    ports:
      - "${MYSQL_PORT:-3306}:3306"
    volumes:
      - ./docker/data/mysql:/var/lib/mysql
    healthcheck:
      test: ["CMD-SHELL", "mysqladmin ping -h 127.0.0.1 -uroot -p$$MYSQL_ROOT_PASSWORD || exit 1"]
      interval: 10s
      timeout: 5s
      retries: 12
      start_period: 30s

  redis:
    image: redis:7-alpine
    container_name: mars-redis
    restart: unless-stopped
    environment:
      REDIS_PASSWORD: ${REDIS_PASSWORD:-}
      TZ: Asia/Shanghai
    command:
      - /bin/sh
      - -c
      - |
        if [ -n "$$REDIS_PASSWORD" ]; then
          exec redis-server --appendonly yes --requirepass "$$REDIS_PASSWORD";
        fi
        exec redis-server --appendonly yes
    ports:
      - "${REDIS_PORT:-6379}:6379"
    volumes:
      - ./docker/data/redis:/data
    healthcheck:
      test: ["CMD-SHELL", "if [ -n \"$$REDIS_PASSWORD\" ]; then redis-cli -a \"$$REDIS_PASSWORD\" ping; else redis-cli ping; fi"]
      interval: 10s
      timeout: 5s
      retries: 10

  app:
    image: "${APP_IMAGE:-ghcr.io/zeezeng/cloud-224}:${APP_IMAGE_TAG:-latest}"
    container_name: mars-app
    restart: unless-stopped
    ports:
      - "${APP_PORT:-8080}:8080"
    environment:
      SPRING_PROFILES_ACTIVE: prod
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/${MYSQL_DATABASE:-mars-system}?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=GMT%2B8
      SPRING_DATASOURCE_USERNAME: root
      SPRING_DATASOURCE_PASSWORD: ${MYSQL_ROOT_PASSWORD:-root}
      SPRING_DATA_REDIS_HOST: redis
      SPRING_DATA_REDIS_PORT: 6379
      SPRING_DATA_REDIS_PASSWORD: ${REDIS_PASSWORD:-}
      SPRING_DATA_REDIS_DATABASE: ${REDIS_DATABASE:-10}
      JAVA_OPTS: ${JAVA_OPTS:--Xms512m -Xmx512m -Dfile.encoding=UTF-8}
      MARS_DEMO_MODE: ${MARS_DEMO_MODE:-false}
      APP_LOG_LEVEL: ${APP_LOG_LEVEL:-info}
      TZ: Asia/Shanghai
    volumes:
      - ./docker/data/uploads:/app/uploads
    depends_on:
      mysql:
        condition: service_healthy
      redis:
        condition: service_healthy
    healthcheck:
      test: ["CMD-SHELL", "curl -fsS http://127.0.0.1:8080/actuator/health >/dev/null || exit 1"]
      interval: 15s
      timeout: 10s
      retries: 12
      start_period: 120s
EOF
}

ensure_registry_login() {
  if [[ -n "${GHCR_USERNAME}" && -n "${GHCR_TOKEN}" ]]; then
    log "登录 GHCR"
    echo "${GHCR_TOKEN}" | need_root_cmd docker login ghcr.io -u "${GHCR_USERNAME}" --password-stdin
  fi
}

compose_pull() {
  log "拉取镜像"
  (
    cd "${APP_DIR}"
    need_root_cmd docker compose pull
  )
}

compose_up() {
  log "启动服务"
  (
    cd "${APP_DIR}"
    need_root_cmd docker compose up -d --remove-orphans
  )
}

wait_for_app() {
  local attempt

  for attempt in $(seq 1 60); do
    local status
    status="$(need_root_cmd docker inspect --format '{{if .State.Health}}{{.State.Health.Status}}{{else}}{{.State.Status}}{{end}}' mars-app 2>/dev/null || true)"
    if [[ "${status}" == "healthy" ]]; then
      log "应用容器状态：${status}"
      return
    fi
    sleep 5
  done

  log "应用仍在启动中，可继续查看日志"
}

show_summary() {
  log "部署完成"
  echo "目录：${APP_DIR}"
  echo "镜像：${APP_IMAGE}:${APP_IMAGE_TAG}"
  echo "访问地址：http://<服务器IP>:${APP_PORT}"
  echo "日志：cd ${APP_DIR} && sudo docker compose logs -f app"
  echo "更新：cd ${APP_DIR} && sudo docker compose pull && sudo docker compose up -d"
}

main() {
  ensure_docker
  mkdir -p "${APP_DIR}/docker/data/mysql" "${APP_DIR}/docker/data/redis" "${APP_DIR}/docker/data/uploads"
  write_env
  write_compose
  ensure_registry_login
  compose_pull
  compose_up
  wait_for_app
  show_summary
}

main "$@"

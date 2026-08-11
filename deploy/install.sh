#!/usr/bin/env bash

set -Eeuo pipefail

APP_DIR="${APP_DIR:-/opt/cloud-224}"
REPO_URL="${REPO_URL:-https://github.com/zeezeng/cloud-224.git}"
BRANCH="${BRANCH:-master}"
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
    echo "未识别的包管理器，请先手动安装 git / docker / curl。" >&2
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

ensure_git() {
  if command -v git >/dev/null 2>&1; then
    return
  fi

  log "未检测到 git，正在安装"
  install_packages git
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

git_sync() {
  if [[ -d "${APP_DIR}/.git" ]]; then
    log "检测到已有部署目录，正在更新代码"
    git -C "${APP_DIR}" fetch --all --prune
    git -C "${APP_DIR}" checkout "${BRANCH}" || git -C "${APP_DIR}" checkout -b "${BRANCH}" "origin/${BRANCH}"
    git -C "${APP_DIR}" pull --ff-only origin "${BRANCH}"
    return
  fi

  if [[ -d "${APP_DIR}" ]] && [[ -n "$(find "${APP_DIR}" -mindepth 1 -maxdepth 1 2>/dev/null)" ]]; then
    echo "部署目录 ${APP_DIR} 已存在且非空，但不是 git 仓库，请先清理后重试。" >&2
    exit 1
  fi

  log "正在克隆代码到 ${APP_DIR}"
  mkdir -p "${APP_DIR}"
  git clone --depth 1 --branch "${BRANCH}" "${REPO_URL}" "${APP_DIR}"
}

prepare_env() {
  local env_path="${APP_DIR}/.env"
  local example_path="${APP_DIR}/.env.example"

  if [[ -f "${env_path}" ]]; then
    log "检测到 .env 已存在，保留现有配置"
    return
  fi

  if [[ ! -f "${example_path}" ]]; then
    echo "缺少 ${example_path}，无法生成环境文件。" >&2
    exit 1
  fi

  cp "${example_path}" "${env_path}"
  log "已生成 ${env_path}，如需自定义端口或密码，请编辑后重新执行脚本"
}

compose_up() {
  log "开始构建并启动容器"
  (
    cd "${APP_DIR}"
    need_root_cmd docker compose up -d --build --remove-orphans
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
  echo "项目目录：${APP_DIR}"
  echo "代码仓库：${REPO_URL}"
  echo "访问地址：http://<服务器IP>:${APP_PORT}"
  echo "查看日志：cd ${APP_DIR} && sudo docker compose logs -f app"
  echo "更新部署：cd ${APP_DIR} && sudo docker compose up -d --build"
}

main() {
  ensure_git
  ensure_docker
  git_sync
  prepare_env
  compose_up
  wait_for_app
  show_summary
}

main "$@"

#!/usr/bin/env bash

set -Eeuo pipefail

APP_DIR="${APP_DIR:-/opt/cloud-224}"
REPO_RAW_BASE="${REPO_RAW_BASE:-https://raw.githubusercontent.com/zeezeng/cloud-224/master}"

APP_IMAGE_DEFAULT="ghcr.io/your-github-username/cloud-224"
APP_IMAGE_TAG_DEFAULT="latest"
APP_PORT_DEFAULT="8080"
JAVA_OPTS_DEFAULT="-Xms512m -Xmx512m -Dfile.encoding=UTF-8"
MARS_DEMO_MODE_DEFAULT="false"
APP_LOG_LEVEL_DEFAULT="info"
MYSQL_ROOT_PASSWORD_DEFAULT="root"
MYSQL_DATABASE_DEFAULT="mars-system"
MYSQL_PORT_DEFAULT="3306"
REDIS_PASSWORD_DEFAULT=""
REDIS_PORT_DEFAULT="6379"
REDIS_DATABASE_DEFAULT="10"

APP_IMAGE="${APP_IMAGE:-${APP_IMAGE_DEFAULT}}"
APP_IMAGE_TAG="${APP_IMAGE_TAG:-${APP_IMAGE_TAG_DEFAULT}}"
APP_PORT="${APP_PORT:-${APP_PORT_DEFAULT}}"
JAVA_OPTS="${JAVA_OPTS:-${JAVA_OPTS_DEFAULT}}"
MARS_DEMO_MODE="${MARS_DEMO_MODE:-${MARS_DEMO_MODE_DEFAULT}}"
APP_LOG_LEVEL="${APP_LOG_LEVEL:-${APP_LOG_LEVEL_DEFAULT}}"
GHCR_USERNAME="${GHCR_USERNAME:-}"
GHCR_TOKEN="${GHCR_TOKEN:-}"
MYSQL_ROOT_PASSWORD="${MYSQL_ROOT_PASSWORD:-${MYSQL_ROOT_PASSWORD_DEFAULT}}"
MYSQL_DATABASE="${MYSQL_DATABASE:-${MYSQL_DATABASE_DEFAULT}}"
MYSQL_PORT="${MYSQL_PORT:-${MYSQL_PORT_DEFAULT}}"
REDIS_PASSWORD="${REDIS_PASSWORD:-${REDIS_PASSWORD_DEFAULT}}"
REDIS_PORT="${REDIS_PORT:-${REDIS_PORT_DEFAULT}}"
REDIS_DATABASE="${REDIS_DATABASE:-${REDIS_DATABASE_DEFAULT}}"

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

ensure_base_packages() {
  local packages=()

  if ! command -v curl >/dev/null 2>&1; then
    packages+=(curl)
  fi

  if [[ "${#packages[@]}" -gt 0 ]]; then
    log "安装基础依赖：${packages[*]}"
    install_packages "${packages[@]}"
  fi
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

ensure_app_dir() {
  need_root_cmd mkdir -p "${APP_DIR}"
}

load_existing_env() {
  local env_file
  env_file="${APP_DIR}/.env"

  if [[ ! -f "${env_file}" ]]; then
    return
  fi

  log "读取已有环境配置 ${env_file}"

  while IFS='=' read -r key value; do
    [[ -n "${key}" ]] || continue
    [[ "${key}" =~ ^[A-Z0-9_]+$ ]] || continue

    case "${key}" in
      APP_IMAGE)
        [[ "${APP_IMAGE}" != "${APP_IMAGE_DEFAULT}" ]] || APP_IMAGE="${value}"
        ;;
      APP_IMAGE_TAG)
        [[ "${APP_IMAGE_TAG}" != "${APP_IMAGE_TAG_DEFAULT}" ]] || APP_IMAGE_TAG="${value}"
        ;;
      APP_PORT)
        [[ "${APP_PORT}" != "${APP_PORT_DEFAULT}" ]] || APP_PORT="${value}"
        ;;
      JAVA_OPTS)
        [[ "${JAVA_OPTS}" != "${JAVA_OPTS_DEFAULT}" ]] || JAVA_OPTS="${value}"
        ;;
      MARS_DEMO_MODE)
        [[ "${MARS_DEMO_MODE}" != "${MARS_DEMO_MODE_DEFAULT}" ]] || MARS_DEMO_MODE="${value}"
        ;;
      APP_LOG_LEVEL)
        [[ "${APP_LOG_LEVEL}" != "${APP_LOG_LEVEL_DEFAULT}" ]] || APP_LOG_LEVEL="${value}"
        ;;
      GHCR_USERNAME)
        [[ -n "${GHCR_USERNAME}" ]] || GHCR_USERNAME="${value}"
        ;;
      GHCR_TOKEN)
        [[ -n "${GHCR_TOKEN}" ]] || GHCR_TOKEN="${value}"
        ;;
      MYSQL_ROOT_PASSWORD)
        [[ "${MYSQL_ROOT_PASSWORD}" != "${MYSQL_ROOT_PASSWORD_DEFAULT}" ]] || MYSQL_ROOT_PASSWORD="${value}"
        ;;
      MYSQL_DATABASE)
        [[ "${MYSQL_DATABASE}" != "${MYSQL_DATABASE_DEFAULT}" ]] || MYSQL_DATABASE="${value}"
        ;;
      MYSQL_PORT)
        [[ "${MYSQL_PORT}" != "${MYSQL_PORT_DEFAULT}" ]] || MYSQL_PORT="${value}"
        ;;
      REDIS_PASSWORD)
        [[ "${REDIS_PASSWORD}" != "${REDIS_PASSWORD_DEFAULT}" ]] || REDIS_PASSWORD="${value}"
        ;;
      REDIS_PORT)
        [[ "${REDIS_PORT}" != "${REDIS_PORT_DEFAULT}" ]] || REDIS_PORT="${value}"
        ;;
      REDIS_DATABASE)
        [[ "${REDIS_DATABASE}" != "${REDIS_DATABASE_DEFAULT}" ]] || REDIS_DATABASE="${value}"
        ;;
    esac
  done < "${env_file}"
}

download_deploy_files() {
  log "下载部署文件到 ${APP_DIR}"
  need_root_cmd curl -fsSL "${REPO_RAW_BASE}/docker-compose.yml" -o "${APP_DIR}/docker-compose.yml"
  need_root_cmd curl -fsSL "${REPO_RAW_BASE}/.env.example" -o "${APP_DIR}/.env.example"
}

write_env() {
  local tmp_env
  tmp_env="$(mktemp)"

  cat > "${tmp_env}" <<EOF
APP_PORT=${APP_PORT}
APP_IMAGE=${APP_IMAGE}
APP_IMAGE_TAG=${APP_IMAGE_TAG}
JAVA_OPTS=${JAVA_OPTS}
MARS_DEMO_MODE=${MARS_DEMO_MODE}
APP_LOG_LEVEL=${APP_LOG_LEVEL}
GHCR_USERNAME=${GHCR_USERNAME}
GHCR_TOKEN=${GHCR_TOKEN}
MYSQL_ROOT_PASSWORD=${MYSQL_ROOT_PASSWORD}
MYSQL_DATABASE=${MYSQL_DATABASE}
MYSQL_PORT=${MYSQL_PORT}
REDIS_PASSWORD=${REDIS_PASSWORD}
REDIS_PORT=${REDIS_PORT}
REDIS_DATABASE=${REDIS_DATABASE}
EOF

  need_root_cmd install -m 600 "${tmp_env}" "${APP_DIR}/.env"
  log "已更新 ${APP_DIR}/.env"

  rm -f "${tmp_env}"
}

ensure_runtime_dirs() {
  need_root_cmd mkdir -p \
    "${APP_DIR}/docker/data/mysql" \
    "${APP_DIR}/docker/data/redis" \
    "${APP_DIR}/docker/data/uploads"
}

login_ghcr() {
  if [[ -z "${GHCR_USERNAME}" || -z "${GHCR_TOKEN}" ]]; then
    log "未提供 GHCR_USERNAME / GHCR_TOKEN，跳过 ghcr.io 登录"
    return
  fi

  log "登录 ghcr.io"
  printf '%s' "${GHCR_TOKEN}" | need_root_cmd docker login ghcr.io -u "${GHCR_USERNAME}" --password-stdin
}

compose_up() {
  log "拉取 GHCR 镜像并启动服务"
  (
    cd "${APP_DIR}"
    need_root_cmd docker compose pull app
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
  echo "部署文件：${REPO_RAW_BASE}"
  echo "镜像：${APP_IMAGE}:${APP_IMAGE_TAG}"
  echo "访问地址：http://<服务器IP>:${APP_PORT}"
  echo "日志：cd ${APP_DIR} && sudo docker compose logs -f app"
  echo "更新：重新执行当前 install.sh，或手动执行 cd ${APP_DIR} && sudo docker compose pull app && sudo docker compose up -d"
}

main() {
  ensure_base_packages
  ensure_docker
  ensure_app_dir
  load_existing_env
  download_deploy_files
  write_env
  ensure_runtime_dirs
  login_ghcr
  compose_up
  wait_for_app
  show_summary
}

main "$@"

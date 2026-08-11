#!/usr/bin/env bash

set -Eeuo pipefail

APP_DIR="${APP_DIR:-/opt/cloud-224}"
GIT_REPO_URL="${GIT_REPO_URL:-https://gitee.com/382444017/cloud-224.git}"
GIT_BRANCH="${GIT_BRANCH:-master}"

APP_IMAGE="${APP_IMAGE:-cloud-224}"
APP_IMAGE_TAG="${APP_IMAGE_TAG:-local}"
APP_PORT="${APP_PORT:-8080}"
JAVA_OPTS="${JAVA_OPTS:--Xms512m -Xmx512m -Dfile.encoding=UTF-8}"
MARS_DEMO_MODE="${MARS_DEMO_MODE:-false}"
APP_LOG_LEVEL="${APP_LOG_LEVEL:-info}"

NODE_REGISTRY_URL="${NODE_REGISTRY_URL:-https://registry.npmmirror.com}"
MAVEN_MIRROR_ID="${MAVEN_MIRROR_ID:-custom-maven-mirror}"
MAVEN_MIRROR_URL="${MAVEN_MIRROR_URL:-https://repo.maven.apache.org/maven2}"

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
    echo "未识别的包管理器，请先手动安装 docker / git / curl。" >&2
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

  if ! command -v git >/dev/null 2>&1; then
    packages+=(git)
  fi
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

sync_repo() {
  local app_parent
  app_parent="$(dirname "${APP_DIR}")"

  if [[ -d "${APP_DIR}/.git" ]]; then
    log "更新 Git 代码：${GIT_REPO_URL} (${GIT_BRANCH})"
    need_root_cmd git -C "${APP_DIR}" remote set-url origin "${GIT_REPO_URL}"
    need_root_cmd git -C "${APP_DIR}" fetch --prune origin "+refs/heads/${GIT_BRANCH}:refs/remotes/origin/${GIT_BRANCH}"
    need_root_cmd git -C "${APP_DIR}" checkout -B "${GIT_BRANCH}" "origin/${GIT_BRANCH}"
    need_root_cmd git -C "${APP_DIR}" reset --hard "origin/${GIT_BRANCH}"
    return
  fi

  if [[ -e "${APP_DIR}" ]] && [[ -n "$(find "${APP_DIR}" -mindepth 1 -maxdepth 1 -print -quit 2>/dev/null)" ]]; then
    echo "${APP_DIR} 已存在且不是 Git 仓库，请先备份并迁移该目录，或指定新的 APP_DIR。" >&2
    exit 1
  fi

  log "克隆 Git 代码：${GIT_REPO_URL} (${GIT_BRANCH})"
  need_root_cmd mkdir -p "${app_parent}"
  need_root_cmd git clone --branch "${GIT_BRANCH}" "${GIT_REPO_URL}" "${APP_DIR}"
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
NODE_REGISTRY_URL=${NODE_REGISTRY_URL}
MAVEN_MIRROR_ID=${MAVEN_MIRROR_ID}
MAVEN_MIRROR_URL=${MAVEN_MIRROR_URL}
MYSQL_ROOT_PASSWORD=${MYSQL_ROOT_PASSWORD}
MYSQL_DATABASE=${MYSQL_DATABASE}
MYSQL_PORT=${MYSQL_PORT}
REDIS_PASSWORD=${REDIS_PASSWORD}
REDIS_PORT=${REDIS_PORT}
REDIS_DATABASE=${REDIS_DATABASE}
EOF

  if [[ ! -f "${APP_DIR}/.env" ]]; then
    need_root_cmd install -m 600 "${tmp_env}" "${APP_DIR}/.env"
    log "已生成 ${APP_DIR}/.env"
  else
    log "检测到 .env 已存在，保留现有配置"
  fi

  rm -f "${tmp_env}"
}

ensure_runtime_dirs() {
  need_root_cmd mkdir -p \
    "${APP_DIR}/docker/data/mysql" \
    "${APP_DIR}/docker/data/redis" \
    "${APP_DIR}/docker/data/uploads"
}

compose_up() {
  log "服务器构建镜像并启动服务"
  (
    cd "${APP_DIR}"
    need_root_cmd docker compose -f docker-compose.yml -f docker-compose.build.yml up -d --build --remove-orphans
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
  echo "代码：${GIT_REPO_URL} (${GIT_BRANCH})"
  echo "镜像：${APP_IMAGE}:${APP_IMAGE_TAG}"
  echo "访问地址：http://<服务器IP>:${APP_PORT}"
  echo "日志：cd ${APP_DIR} && sudo docker compose logs -f app"
  echo "更新：cd ${APP_DIR} && sudo git fetch --prune origin +refs/heads/${GIT_BRANCH}:refs/remotes/origin/${GIT_BRANCH} && sudo git checkout -B ${GIT_BRANCH} origin/${GIT_BRANCH} && sudo git reset --hard origin/${GIT_BRANCH} && sudo docker compose -f docker-compose.yml -f docker-compose.build.yml up -d --build"
}

main() {
  ensure_base_packages
  ensure_docker
  sync_repo
  write_env
  ensure_runtime_dirs
  compose_up
  wait_for_app
  show_summary
}

main "$@"

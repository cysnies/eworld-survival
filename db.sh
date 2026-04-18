#!/usr/bin/env bash
# 生存服数据库初始化与迁移。依据本目录 .env，通过 Docker 的 mysql:8.0 客户端
#（或 USE_LOCAL_MYSQL_CLI=1 时使用本机 mysql）连接 MySQL。
#
# 用法（项目根目录）:
#   chmod +x db.sh
#   ./db.sh help
#   ./db.sh ensure          # CREATE DATABASE + 应用账号（mysql_native_password）
#   ./db.sh init-schema     # 执行 sql/schema/init_mc001.sql
#   ./db.sh import [文件]   # 导入 dump（默认 .env 中 MIGRATION_SQL，多为 sql/dumps 下文件）
#   ./db.sh bootstrap       # ensure + init-schema（空库初始化）
#
set -euo pipefail

ROOT="$(cd "$(dirname "$0")" && pwd)"
cd "$ROOT"

die() { echo "错误: $*" >&2; exit 1; }

if [ ! -f .env ]; then
  die "未找到 ${ROOT}/.env，请从 env.example 复制并填写。"
fi

set -a
# shellcheck disable=SC1091
source .env
set +a

MYSQL_DATABASE="${MYSQL_DATABASE:-mc001}"
MYSQL_USER="${MYSQL_USER:-mc001}"
MYSQL_PASSWORD="${MYSQL_PASSWORD:?请在 .env 中设置 MYSQL_PASSWORD}"
MYSQL_ROOT_PASSWORD="${MYSQL_ROOT_PASSWORD:?请在 .env 中设置 MYSQL_ROOT_PASSWORD}"

USE_LOCAL_MYSQL_CLI="${USE_LOCAL_MYSQL_CLI:-0}"

SCHEMA_FILE="${ROOT}/sql/schema/init_mc001.sql"

_sql_escape() {
  printf '%s' "$1" | sed "s/'/''/g"
}

compose_mysql_up() {
  docker compose ps --services --status running 2>/dev/null | grep -qx 'mysql'
}

local_mysql_conn() {
  local ch="${MIGRATE_MYSQL_HOST:-127.0.0.1}"
  local cp="${MIGRATE_MYSQL_PORT:-${MYSQL_PUBLISH_PORT:-${MYSQL_PORT:-33060}}}"
  printf '%s %s' "$ch" "$cp"
}

mysql_pipe() {
  if [ "$USE_LOCAL_MYSQL_CLI" = "1" ] && command -v mysql >/dev/null 2>&1; then
    read -r ch cp <<<"$(local_mysql_conn)"
    mysql -h"$ch" -P"$cp" -uroot -p"$MYSQL_ROOT_PASSWORD"
    return
  fi

  if compose_mysql_up; then
    docker compose exec -T mysql mysql -uroot -p"$MYSQL_ROOT_PASSWORD"
    return
  fi

  local h="${MYSQL_HOST:-host.docker.internal}"
  local p="${MYSQL_PORT:-33060}"
  docker run --rm -i \
    --add-host=host.docker.internal:host-gateway \
    -e MYSQL_ROOT_PASSWORD="$MYSQL_ROOT_PASSWORD" \
    -e MYSQLH="$h" \
    -e MYSQLP="$p" \
    mysql:8.0 \
    sh -c 'exec mysql -h"$MYSQLH" -P"$MYSQLP" -uroot -p"$MYSQL_ROOT_PASSWORD"'
}

mysql_file() {
  local f="$1"
  [ -f "$f" ] || die "找不到 SQL 文件: $f"

  if [ "$USE_LOCAL_MYSQL_CLI" = "1" ] && command -v mysql >/dev/null 2>&1; then
    read -r ch cp <<<"$(local_mysql_conn)"
    mysql -h"$ch" -P"$cp" -uroot -p"$MYSQL_ROOT_PASSWORD" <"$f"
    return
  fi

  if compose_mysql_up; then
    docker compose exec -T mysql mysql -uroot -p"$MYSQL_ROOT_PASSWORD" <"$f"
    return
  fi

  local h="${MYSQL_HOST:-host.docker.internal}"
  local p="${MYSQL_PORT:-33060}"
  docker run --rm \
    --add-host=host.docker.internal:host-gateway \
    -e MYSQL_ROOT_PASSWORD="$MYSQL_ROOT_PASSWORD" \
    -e MYSQLH="$h" \
    -e MYSQLP="$p" \
    -v "$f:/tmp/migrate.sql:ro" \
    mysql:8.0 \
    sh -c 'mysql -h"$MYSQLH" -P"$MYSQLP" -uroot -p"$MYSQL_ROOT_PASSWORD" < /tmp/migrate.sql'
}

cmd_ensure() {
  local esc
  esc="$(_sql_escape "$MYSQL_PASSWORD")"
  echo "创建库 ${MYSQL_DATABASE} 与用户 ${MYSQL_USER}（若不存在）..."
  {
    echo "CREATE DATABASE IF NOT EXISTS \`${MYSQL_DATABASE}\` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
    echo "CREATE USER IF NOT EXISTS '${MYSQL_USER}'@'%' IDENTIFIED WITH mysql_native_password BY '${esc}';"
    echo "GRANT ALL PRIVILEGES ON \`${MYSQL_DATABASE}\`.* TO '${MYSQL_USER}'@'%';"
    echo "FLUSH PRIVILEGES;"
  } | mysql_pipe
  echo "ensure 完成。"
}

cmd_init_schema() {
  [ -f "$SCHEMA_FILE" ] || die "未找到 $SCHEMA_FILE"
  echo "导入表结构: $SCHEMA_FILE"
  mysql_file "$SCHEMA_FILE"
  echo "init-schema 完成。"
}

cmd_import() {
  local file="${1:-${MIGRATION_SQL:-}}"
  [ -n "$file" ] || die "请指定 SQL 文件，或设置 .env 中 MIGRATION_SQL（例如 sql/dumps/xxx.sql）"
  [ -f "$file" ] || die "找不到文件: $file"
  local abs
  abs="$(cd "$(dirname "$file")" && pwd)/$(basename "$file")"
  echo "导入迁移数据: $abs"
  mysql_file "$abs"
  echo "import 完成。"
}

cmd_bootstrap() {
  cmd_ensure
  cmd_init_schema
  echo "bootstrap 完成。若有 phpStudy 全量 dump，可再执行: $0 import sql/dumps/xxx.sql"
}

cmd_help() {
  sed -n '2,11p' "$0"
}

main() {
  case "${1:-help}" in
    ensure)         cmd_ensure ;;
    init-schema)    cmd_init_schema ;;
    import|migrate) shift; cmd_import "${1:-}" ;;
    bootstrap)      cmd_bootstrap ;;
    help|-h|--help) cmd_help ;;
    *)              die "未知子命令: ${1:-}。请运行: $0 help" ;;
  esac
}

main "$@"

#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")" && pwd)"
cd "$ROOT"

LOG_DIR="$ROOT/logs"
mkdir -p "$LOG_DIR" \
  "$ROOT/data/uploads" \
  "$ROOT/traffic_access_core/src/main/java/key" \
  "$ROOT/privacy_policy_controller/src/main/java/key" \
  "$ROOT/traffic_stream_producer/src/main/java/key"

BUILD=0
STOP_EXISTING=0

usage() {
  cat <<EOF
Usage: bash start-backend.sh [--build] [--stop-existing]

Starts all Java backend services:
  - secure_stream_store          : 1101
  - privacy_policy_controller    : 1102
  - traffic_stream_producer      : 1234
  - web_gateway                  : 8080

Options:
  --build          Build backend jars with Maven before starting.
  --stop-existing  Kept for compatibility. Existing managed services are now
                   detected and restarted automatically by default.
EOF
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --build) BUILD=1 ;;
    --stop-existing) STOP_EXISTING=1 ;;
    -h|--help) usage; exit 0 ;;
    *) echo "Unknown option: $1"; usage; exit 1 ;;
  esac
  shift
done

require_cmd() {
  if ! command -v "$1" >/dev/null 2>&1; then
    echo "Missing command: $1"
    exit 1
  fi
}

port_pid() {
  local port="$1"
  lsof -tiTCP:"$port" -sTCP:LISTEN 2>/dev/null | head -n 1 || true
}

wait_for_port() {
  local name="$1"
  local port="$2"
  local deadline=$((SECONDS + 30))
  while [[ $SECONDS -lt $deadline ]]; do
    if [[ -n "$(port_pid "$port")" ]]; then
      echo "$name is listening on :$port"
      return 0
    fi
    sleep 1
  done
  echo "WARN: $name did not open :$port within 30s. Check $LOG_DIR/${name}.log"
}

service_is_running() {
  local name="$1"
  local port="$2"
  local pid_file="$LOG_DIR/${name}.pid"
  if [[ -f "$pid_file" ]] && kill -0 "$(cat "$pid_file")" 2>/dev/null; then
    return 0
  fi
  [[ -n "$(port_pid "$port")" ]]
}

is_managed_process() {
  local pid="$1"
  local jar="$2"
  local command_line
  command_line="$(ps -p "$pid" -o command= 2>/dev/null || true)"
  [[ "$command_line" == *"$(basename "$jar")"* ]]
}

wait_for_stop() {
  local name="$1"
  local port="$2"
  local pid="$3"
  local deadline=$((SECONDS + 12))
  while [[ $SECONDS -lt $deadline ]]; do
    if ! kill -0 "$pid" 2>/dev/null && [[ -z "$(port_pid "$port")" ]]; then
      return 0
    fi
    sleep 1
  done
  echo "Stopping $name forcefully, PID=$pid ..."
  kill -9 "$pid" 2>/dev/null || true
}

stop_service() {
  local name="$1"
  local jar="$2"
  local port="$3"
  local pid_file="$LOG_DIR/${name}.pid"
  local recorded_pid=""
  if [[ -f "$pid_file" ]]; then
    recorded_pid="$(cat "$pid_file")"
    if kill -0 "$recorded_pid" 2>/dev/null; then
      echo "Stopping $name PID=$recorded_pid ..."
      kill "$recorded_pid" 2>/dev/null || true
      wait_for_stop "$name" "$port" "$recorded_pid"
    fi
    rm -f "$pid_file"
  fi

  local listening_pid
  listening_pid="$(port_pid "$port")"
  if [[ -n "$listening_pid" ]] && [[ "$listening_pid" != "$recorded_pid" ]]; then
    if is_managed_process "$listening_pid" "$jar"; then
      echo "Stopping detected $name PID=$listening_pid on :$port ..."
      kill "$listening_pid" 2>/dev/null || true
      wait_for_stop "$name" "$port" "$listening_pid"
    else
      echo "WARN: port $port is occupied by an unmanaged process PID=$listening_pid; it will not be stopped."
    fi
  fi
}

start_service() {
  local name="$1"
  local module="$2"
  local jar="$3"
  local port="$4"
  local pid_file="$LOG_DIR/${name}.pid"
  local log_file="$LOG_DIR/${name}.log"

  if [[ -f "$pid_file" ]] && kill -0 "$(cat "$pid_file")" 2>/dev/null; then
    echo "$name already running, PID=$(cat "$pid_file")"
    return
  fi

  local existing_pid
  existing_pid="$(port_pid "$port")"
  if [[ -n "$existing_pid" ]]; then
    echo "ERROR: port $port is already used by PID=$existing_pid. Stop it or run: bash start-backend.sh --stop-existing"
    exit 1
  fi

  if [[ ! -f "$jar" ]]; then
    echo "ERROR: missing jar: $jar"
    echo "Run: bash start-backend.sh --build"
    exit 1
  fi

  echo "Starting $name from $module ..."
  nohup java -jar "$jar" > "$log_file" 2>&1 &
  echo $! > "$pid_file"
  wait_for_port "$name" "$port"
}

require_cmd java
if [[ "$BUILD" -eq 1 ]]; then
  require_cmd mvn
fi
if [[ ! -f "$ROOT/config/database.properties" ]]; then
  echo "ERROR: missing $ROOT/config/database.properties"
  echo "Create it from config/database.properties.example or copy your executable config."
  exit 1
fi

# 安装了项目本地 Ganache 后，启动后端时自动确保开发链可用。
if [[ -x "$ROOT/start-local-chain.sh" ]] && docker image inspect ghcr.io/foundry-rs/foundry:stable >/dev/null 2>&1; then
  bash "$ROOT/start-local-chain.sh"
fi

# 本地开发链配置由 start-local-chain.sh 首次启动时生成，文件不进入版本控制。
if [[ -f "$ROOT/config/blockchain-local.env" ]]; then
  set -a
  # shellcheck disable=SC1091
  source "$ROOT/config/blockchain-local.env"
  set +a
fi

SYSTEM_RUNNING=0
if service_is_running secure_stream_store 1101 \
  || service_is_running privacy_policy_controller 1102 \
  || service_is_running traffic_stream_producer 1234 \
  || service_is_running web_gateway 8080; then
  SYSTEM_RUNNING=1
fi

if [[ "$SYSTEM_RUNNING" -eq 1 || "$STOP_EXISTING" -eq 1 ]]; then
  echo "Existing backend system detected. Restarting all managed services ..."
  stop_service web_gateway "$ROOT/web_gateway/target/web_gateway-1.0-SNAPSHOT.jar" 8080
  stop_service traffic_stream_producer "$ROOT/traffic_stream_producer/target/traffic_stream_producer-1.0-SNAPSHOT.jar" 1234
  stop_service privacy_policy_controller "$ROOT/privacy_policy_controller/target/privacy_policy_controller-1.0-SNAPSHOT.jar" 1102
  stop_service secure_stream_store "$ROOT/secure_stream_store/target/secure_stream_store-1.0-SNAPSHOT.jar" 1101
else
  echo "No running backend system detected. Starting a new backend system ..."
fi

if [[ "$BUILD" -eq 1 ]]; then
  echo "Building backend modules ..."
  mvn -q -pl traffic_access_core install -DskipTests
  mvn -q -pl secure_stream_store package -DskipTests
  mvn -q -pl privacy_policy_controller package -DskipTests
  mvn -q -pl traffic_stream_producer package -DskipTests
  mvn -q -pl web_gateway package -DskipTests
fi

echo "Starting backend services. MySQL and Kafka should already be running."
start_service secure_stream_store secure_stream_store "$ROOT/secure_stream_store/target/secure_stream_store-1.0-SNAPSHOT.jar" 1101
start_service privacy_policy_controller privacy_policy_controller "$ROOT/privacy_policy_controller/target/privacy_policy_controller-1.0-SNAPSHOT.jar" 1102
start_service traffic_stream_producer traffic_stream_producer "$ROOT/traffic_stream_producer/target/traffic_stream_producer-1.0-SNAPSHOT.jar" 1234
start_service web_gateway web_gateway "$ROOT/web_gateway/target/web_gateway-1.0-SNAPSHOT.jar" 8080

echo ""
echo "Backend startup complete."
echo "- secure_stream_store       http://localhost:1101"
echo "- privacy_policy_controller http://localhost:1102"
echo "- traffic_stream_producer   http://localhost:1234"
echo "- web_gateway               http://localhost:8080/api/health"
echo "Logs: $LOG_DIR"

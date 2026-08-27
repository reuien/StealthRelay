#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")" && pwd)"
CHAIN_HOME="$ROOT/data/local-chain"
LOG_DIR="$ROOT/logs"
ENV_FILE="$ROOT/config/blockchain-local.env"
MNEMONIC_FILE="$ROOT/config/anvil-local.secret"
LOG_FILE="$LOG_DIR/local_chain.log"
IMAGE="ghcr.io/foundry-rs/foundry:stable"
CONTAINER="attempt-local-chain"
RPC_PORT=8545
CHAIN_ID=31337
umask 077

mkdir -p "$CHAIN_HOME" "$LOG_DIR"

for command in docker curl jq; do
  if ! command -v "$command" >/dev/null 2>&1; then
    echo "ERROR: 缺少命令 $command"
    exit 1
  fi
done

if ! docker image inspect "$IMAGE" >/dev/null 2>&1; then
  echo "ERROR: 缺少官方 Foundry 镜像 $IMAGE"
  echo "请先运行: docker pull $IMAGE"
  exit 1
fi

if [[ ! -f "$ENV_FILE" || ! -f "$MNEMONIC_FILE" ]]; then
  wallet_json="$(docker run --rm "$IMAGE" 'cast wallet new-mnemonic --words 12 --json')"
  mnemonic="$(printf '%s' "$wallet_json" | jq -r '.mnemonic')"
  private_key="$(printf '%s' "$wallet_json" | jq -r '.accounts[0].private_key')"
  if [[ ! "$private_key" =~ ^0x[0-9a-fA-F]{64}$ ]] || [[ -z "$mnemonic" || "$mnemonic" == "null" ]]; then
    echo "ERROR: Foundry 未返回有效的开发账户"
    exit 1
  fi
  {
    printf 'BLOCKCHAIN_RPC_URL=http://127.0.0.1:%s\n' "$RPC_PORT"
    printf 'BLOCKCHAIN_LOCAL_RPC_URL=http://127.0.0.1:%s\n' "$RPC_PORT"
    printf 'BLOCKCHAIN_PRIVATE_KEY=%s\n' "${private_key#0x}"
    printf 'BLOCKCHAIN_CHAIN_ID=%s\n' "$CHAIN_ID"
    printf 'BLOCKCHAIN_EXPLORER_BASE_URL=\n'
  } > "$ENV_FILE"
  printf '%s' "$mnemonic" > "$MNEMONIC_FILE"
  chmod 600 "$ENV_FILE" "$MNEMONIC_FILE"
  echo "已生成仅供本地 Anvil 使用的随机开发账户"
fi

rpc_ready() {
  curl -fsS -H 'Content-Type: application/json' \
    --data '{"jsonrpc":"2.0","method":"eth_chainId","params":[],"id":1}' \
    "http://127.0.0.1:${RPC_PORT}" >/dev/null 2>&1
}

container_exists() {
  docker container inspect "$CONTAINER" >/dev/null 2>&1
}

container_running() {
  [[ "$(docker inspect -f '{{.State.Running}}' "$CONTAINER" 2>/dev/null || true)" == "true" ]]
}

start_container() {
  local mnemonic
  mnemonic="$(<"$MNEMONIC_FILE")"
  docker run -d \
    --name "$CONTAINER" \
    --restart unless-stopped \
    -p "127.0.0.1:${RPC_PORT}:8545" \
    -v "$CHAIN_HOME:/data" \
    "$IMAGE" \
    "anvil --host 0.0.0.0 --port 8545 --chain-id $CHAIN_ID --accounts 1 --balance 1000 --mnemonic '$mnemonic' --state /data/state.json --state-interval 1 --silent" \
    >/dev/null
}

if container_running && rpc_ready; then
  echo "本地区块链已运行（Anvil，chainId=${CHAIN_ID}）"
  exit 0
fi

if container_exists; then
  docker start "$CONTAINER" >/dev/null
else
  start_container
fi

deadline=$((SECONDS + 30))
while [[ $SECONDS -lt $deadline ]]; do
  if rpc_ready; then
    printf 'Anvil local chain is running on 127.0.0.1:%s (chainId=%s)\n' "$RPC_PORT" "$CHAIN_ID" > "$LOG_FILE"
    chmod 600 "$LOG_FILE"
    echo "本地区块链已启动：http://127.0.0.1:${RPC_PORT}（Anvil，chainId=${CHAIN_ID}）"
    exit 0
  fi
  sleep 1
done

echo "ERROR: Anvil 未在 30 秒内就绪，请运行 docker logs $CONTAINER 检查"
exit 1

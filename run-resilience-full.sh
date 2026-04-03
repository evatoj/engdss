#!/usr/bin/env bash

set -u

NETWORK="observability-net"
TESTS_DIR="$PWD/tests"
K6_IMAGE="grafana/k6"
K6_SCRIPT="/tests/resilience/transaction-ledger.js"

FAIL_AFTER_SECONDS=40
DOWN_TIME_SECONDS=15

CONTAINERS=(
  "ledger-service"
  "rabbitmq"
  "transaction-service"
)

log() {
  echo "[$(date '+%H:%M:%S')] $*"
}

echo "========================================"
echo "TESTE COMPLETO DE RESILIÊNCIA"
echo "Containers testados: ${CONTAINERS[*]}"
echo "========================================"

# validações
if [ ! -d "$TESTS_DIR" ]; then
  echo "ERRO: pasta de testes não encontrada"
  exit 1
fi

if [ ! -f "$TESTS_DIR/resilience/transaction-ledger.js" ]; then
  echo "ERRO: script k6 não encontrado"
  exit 1
fi

if ! docker network inspect "$NETWORK" >/dev/null 2>&1; then
  echo "ERRO: rede $NETWORK não encontrada"
  exit 1
fi

# iniciar k6
log "Iniciando k6..."
docker run --rm \
  --network="$NETWORK" \
  -v "$TESTS_DIR:/tests" \
  "$K6_IMAGE" run "$K6_SCRIPT" &
K6_PID=$!

log "k6 rodando (PID $K6_PID)"

sleep "$FAIL_AFTER_SECONDS"

# loop de falhas
for CONTAINER in "${CONTAINERS[@]}"; do
  log "----------------------------------------"
  log "Testando falha em: $CONTAINER"
  
  log "Derrubando $CONTAINER..."
  docker stop "$CONTAINER"

  log "Aguardando ${DOWN_TIME_SECONDS}s..."
  sleep "$DOWN_TIME_SECONDS"

  log "Subindo $CONTAINER..."
  docker start "$CONTAINER"

  log "Aguardando recuperação (20s)..."
  sleep 20
done

log "----------------------------------------"
log "Aguardando finalização do k6..."

wait "$K6_PID"
K6_EXIT_CODE=$?

log "========================================"
log "Teste finalizado"
log "Exit code: $K6_EXIT_CODE"
log "========================================"

exit "$K6_EXIT_CODE"
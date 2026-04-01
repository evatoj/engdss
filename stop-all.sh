#!/usr/bin/env bash

set -e

echo "🛑 Parando tudo..."

docker compose down

cd observabilidade/signoz/deploy/docker
docker compose down

cd - >/dev/null

echo "✅ Tudo parado."

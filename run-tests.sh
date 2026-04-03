#!/bin/bash

set -e

echo "Rodando transaction health..."
docker run --rm --network=observability-net -v "$PWD/tests:/tests" grafana/k6 run /tests/transaction/health.js

echo "Rodando ledger health..."
docker run --rm --network=observability-net -v "$PWD/tests:/tests" grafana/k6 run /tests/ledger/health.js
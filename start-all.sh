#!/usr/bin/env bash

set -e

echo "🚀 Iniciando ambiente completo..."

# 🔹 1. Subir serviços principais
echo "📦 Subindo serviços do projeto (backend, frontend, banco, rabbit)..."
docker compose build --no-cache
docker compose up -d 

# 🔹 2. Subir SigNoz
echo "📊 Subindo SigNoz..."
cd observabilidade/signoz/deploy/docker
docker compose up -d

cd - >/dev/null

echo ""
echo "✅ Tudo rodando!"
echo ""
echo "🌐 Frontend: http://localhost:4200"
echo "⚙️ Backend: http://localhost:8080"
echo "📊 SigNoz: http://localhost:3301"
echo ""
echo "👀 Agora gere requisições e veja no SigNoz!"

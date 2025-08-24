#!/usr/bin/env bash
set -euo pipefail
echo "[frontend] Node: $(node -v 2>/dev/null || echo not installed)"

if [[ -d node_modules && "${FORCE_INSTALL:-0}" != "1" ]]; then
  echo "[frontend] node_modules exists — skipping install (FORCE_INSTALL=1 to force)"
else
  if [[ -f package-lock.json ]]; then
    npm ci || npm install --legacy-peer-deps
  else
    npm install --legacy-peer-deps
  fi
fi

HOST="${HOST:-0.0.0.0}"
PORT="${PORT:-8081}"
echo "[frontend] dev server: http://localhost:$PORT"
exec npx vue-cli-service serve --host "$HOST" --port "$PORT"

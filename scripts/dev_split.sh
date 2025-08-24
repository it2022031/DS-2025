#!/usr/bin/env bash
set -euo pipefail

# -------- paths --------
ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

if [[ -d "$ROOT_DIR/backend/demo" ]]; then
  BACKEND_DIR="$ROOT_DIR/backend/demo"
else
  BACKEND_DIR="$ROOT_DIR/backend"
fi

if [[ -d "$ROOT_DIR/frontend/vue-argon-design-system-master" ]]; then
  FRONTEND_DIR="$ROOT_DIR/frontend/vue-argon-design-system-master"
else
  FRONTEND_DIR="$ROOT_DIR/frontend"
fi

mkdir -p "$ROOT_DIR/scripts"
BACK_CMD="$ROOT_DIR/scripts/.run_backend.sh"
FRONT_CMD="$ROOT_DIR/scripts/.run_frontend.sh"

# -------- backend helper --------
cat > "$BACK_CMD" <<'BACK'
#!/usr/bin/env bash
set -euo pipefail
if [[ -x ./mvnw ]]; then MVN=./mvnw
elif command -v mvn >/dev/null 2>&1; then MVN=mvn
else echo "ERROR: mvn/mvnw not found"; read -p "Press Enter..."; exit 1; fi

SKIP_TESTS="${SKIP_TESTS:-1}"
if [[ "$SKIP_TESTS" == "1" ]]; then
  echo "[backend] starting (skipTests)"; MVN_ARGS="-DskipTests"
else
  echo "[backend] starting (WITH tests)"; MVN_ARGS=""
fi
exec "$MVN" $MVN_ARGS spring-boot:run
BACK
chmod +x "$BACK_CMD"

# -------- frontend helper --------
cat > "$FRONT_CMD" <<'FRONT'
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
FRONT
chmod +x "$FRONT_CMD"

echo "Project root : $ROOT_DIR"
echo "Backend dir  : $BACKEND_DIR"
echo "Frontend dir : $FRONTEND_DIR"

# -------- open two WINDOWS (όχι tabs) --------
if command -v gnome-terminal >/dev/null 2>&1; then
  gnome-terminal --title="Backend (Spring Boot)" \
    -- bash -lc "cd '$BACKEND_DIR'; SKIP_TESTS='${SKIP_TESTS:-1}' '$BACK_CMD'; exec bash" &
  sleep 1
  gnome-terminal --title="Frontend (Vue)" \
    -- bash -lc "cd '$FRONTEND_DIR'; '$FRONT_CMD'; exec bash" &
elif command -v xterm >/dev/null 2>&1; then
  xterm -T "Backend (Spring Boot)" -e bash -lc "cd '$BACKEND_DIR'; SKIP_TESTS='${SKIP_TESTS:-1}' '$BACK_CMD'; exec bash" &
  xterm -T "Frontend (Vue)"        -e bash -lc "cd '$FRONTEND_DIR'; '$FRONT_CMD'; exec bash" &
elif command -v tmux >/dev/null 2>&1; then
  tmux new-session \; \
    rename-session 'dev' \; \
    send-keys "cd '$BACKEND_DIR'; SKIP_TESTS='${SKIP_TESTS:-1}' '$BACK_CMD'" C-m \; \
    split-window -h \; \
    send-keys "cd '$FRONTEND_DIR'; '$FRONT_CMD'" C-m \; \
    select-pane -L \; \
    attach
else
  echo "No gnome-terminal/xterm/tmux found."
  echo "Run manually in two terminals:"
  echo "  (1) cd '$BACKEND_DIR' && SKIP_TESTS='${SKIP_TESTS:-1}' '$BACK_CMD'"
  echo "  (2) cd '$FRONTEND_DIR' && '$FRONT_CMD'"
fi


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

# -------- detect docker compose flavor --------
DCMD=""
if command -v docker >/dev/null 2>&1; then
  # Try plugin syntax first
  if docker compose version >/dev/null 2>&1; then
    DCMD="docker compose"
  fi
fi
# Fallback to legacy docker-compose binary
if [[ -z "$DCMD" ]] && command -v docker-compose >/dev/null 2>&1; then
  DCMD="docker-compose"
fi

# -------- DB check & (optional) compose up --------
DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-5432}"

db_up=1
if command -v nc >/dev/null 2>&1; then
  if nc -z "$DB_HOST" "$DB_PORT" 2>/dev/null; then db_up=0; fi
elif command -v pg_isready >/dev/null 2>&1; then
  if pg_isready -h "$DB_HOST" -p "$DB_PORT" >/dev/null 2>&1; then db_up=0; fi
fi

if [[ $db_up -eq 0 ]]; then
  echo "✅ Η βάση φαίνεται να τρέχει ήδη στο ${DB_HOST}:${DB_PORT} — δεν σηκώνω docker."
else
  # Βρες ένα docker-compose.yml για τη βάση
  COMPOSE_FILE=""
  for f in \
    "$BACKEND_DIR/src/main/java/com/example/demo/docker-compose.yml" \
    "$BACKEND_DIR/docker-compose.yml" \
    "$ROOT_DIR/docker-compose.yml"
  do
    if [[ -f "$f" ]]; then COMPOSE_FILE="$f"; break; fi
  done

  if [[ -n "$COMPOSE_FILE" ]]; then
    if [[ -z "$DCMD" ]]; then
      echo "❌ Δεν βρέθηκε Docker Compose (ούτε plugin 'docker compose' ούτε binary 'docker-compose')."
      echo "   Εγκατέστησε Docker Compose και ξανατρέξε το script."
      exit 1
    fi
    echo "🟡 DB down — ξεκινάω $DCMD με file: $COMPOSE_FILE"
    # Αν έχεις service name, μπορείς: "$DCMD" -f "$COMPOSE_FILE" up -d db
    $DCMD -f "$COMPOSE_FILE" up -d

    echo "⏳ Περιμένω τη βάση να ανοίξει στο ${DB_HOST}:${DB_PORT}..."
    for i in {1..40}; do
      if command -v nc >/dev/null 2>&1; then
        nc -z "$DB_HOST" "$DB_PORT" 2>/dev/null && { echo "✅ DB UP"; break; }
      elif command -v pg_isready >/dev/null 2>&1; then
        pg_isready -h "$DB_HOST" -p "$DB_PORT" >/dev/null 2>&1 && { echo "✅ DB UP"; break; }
      fi
      sleep 1
      [[ $i -eq 40 ]] && echo "⚠️  Δεν επιβεβαιώθηκε η εκκίνηση της DB (ίσως αργεί λίγο ακόμα)."
    done
  else
    echo "⚠️  Δεν βρέθηκε docker-compose.yml για DB. Συνεχίζω χωρίς να σηκώσω βάση."
  fi
fi

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
  echo "Run manually σε δύο τερματικά:"
  echo "  (1) cd '$BACKEND_DIR' && SKIP_TESTS='${SKIP_TESTS:-1}' '$BACK_CMD'"
  echo "  (2) cd '$FRONTEND_DIR' && '$FRONT_CMD'"
fi


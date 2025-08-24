#!/usr/bin/env bash
set -euo pipefail
#!/usr/bin/env bash
set -euo pipefail

# Πήγαινε στη ρίζα του repo (ο φάκελος που περιέχει backend/ και frontend/)
ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

LOG_DIR="${LOG_DIR:-logs}"
mkdir -p "$LOG_DIR"

echo "📂 Project root: $ROOT_DIR"
echo "🗒 Logs: $LOG_DIR"

################################
#          BACKEND             #
################################

# Αν υπάρχει backend/demo, χρησιμοποίησέ το, αλλιώς backend
if [[ -d "backend/demo" ]]; then
  BACKEND_DIR="${BACKEND_DIR:-backend/demo}"
else
  BACKEND_DIR="${BACKEND_DIR:-backend}"
fi

echo "▶ Starting backend in: $BACKEND_DIR"
pushd "$BACKEND_DIR" >/dev/null

# Διάλεξε mvnw αν υπάρχει, αλλιώς system mvn
if [[ -x "./mvnw" ]]; then
  MVN="./mvnw"
elif command -v mvn >/dev/null 2>&1; then
  MVN="mvn"
else
  echo "❌ Δεν βρέθηκε mvn/mvnw. Εγκατάστησε Maven ή χρησιμοποίησε τον wrapper (./mvnw)."
  exit 1
fi

# Αν το pom έχει spring-boot-maven-plugin, τρέξε spring-boot:run
if grep -q "<artifactId>spring-boot-maven-plugin</artifactId>" pom.xml; then
  echo "🚀 mvn spring-boot:run (skipTests)"
  $MVN -q -DskipTests spring-boot:run > "$ROOT_DIR/$LOG_DIR/backend.log" 2>&1 &
else
  echo "🛠  mvn package (skipTests) και μετά java -jar target/*.jar"
  $MVN -q -DskipTests package > "$ROOT_DIR/$LOG_DIR/backend.log" 2>&1
  JAR=$(ls target/*SNAPSHOT*.jar 2>/dev/null || ls target/*.jar 2>/dev/null | head -n1)
  if [[ -z "${JAR:-}" ]]; then
    echo "❌ Δεν βρέθηκε jar στο target/"
    exit 1
  fi
  nohup java -jar "$JAR" > "$ROOT_DIR/$LOG_DIR/backend.log" 2>&1 &
fi

BACK_PID=$!
popd >/dev/null
echo "✅ Backend PID: $BACK_PID  (logs: $LOG_DIR/backend.log)"

# Καθάρισμα backend όταν τερματίσει το script
cleanup() {
  echo "🧹 Stopping backend (PID $BACK_PID)"
  kill "$BACK_PID" >/dev/null 2>&1 || true
}
trap cleanup EXIT INT TERM

# Προαιρετικά: περίμενε να ανοίξει πόρτα backend (default 8080)
BACKEND_PORT="${BACKEND_PORT:-8080}"
echo -n "⏳ Waiting for backend on port $BACKEND_PORT "
for i in {1..60}; do
  if (echo >/dev/tcp/127.0.0.1/$BACKEND_PORT) >/dev/null 2>&1; then
    echo "✓"
    break
  fi
  echo -n "."
  sleep 1
done

################################
#          FRONTEND            #
################################

# Αν υπάρχει frontend/vue-argon-design-system-master, χρησιμοποίησέ το
if [[ -d "frontend/vue-argon-design-system-master" ]]; then
  FRONTEND_DIR="${FRONTEND_DIR:-frontend/vue-argon-design-system-master}"
else
  FRONTEND_DIR="${FRONTEND_DIR:-frontend}"
fi

echo "▶ Starting frontend in: $FRONTEND_DIR"
pushd "$FRONTEND_DIR" >/dev/null

echo "Node: $(node -v 2>/dev/null || echo 'not installed')"

# Μην κάνεις εγκατάσταση κάθε φορά: αν υπάρχει node_modules, προχώρα.
# Θέλεις να αναγκάσεις εγκατάσταση; τρέξε: FORCE_INSTALL=1 ./scripts/dev_all.sh
if [[ -d node_modules && "${FORCE_INSTALL:-0}" != "1" ]]; then
  echo "📦 node_modules υπάρχει — skip npm install (FORCE_INSTALL=1 για αναγκαστική εγκατάσταση)"
else
  if [[ -f package-lock.json ]]; then
    npm ci || npm install --legacy-peer-deps
  else
    npm install --legacy-peer-deps
  fi
fi

HOST="${HOST:-0.0.0.0}"
PORT="${PORT:-8081}"

echo "🌐 Frontend: http://localhost:$PORT"
# Τρέξε dev server και γράφε logs
npx vue-cli-service serve --host "$HOST" --port "$PORT" 2>&1 | tee "$ROOT_DIR/$LOG_DIR/frontend.log"
FRONT_STATUS=${PIPESTATUS[0]}

popd >/dev/null

echo "⛔ Frontend exited with status $FRONT_STATUS"
exit $FRONT_STATUS

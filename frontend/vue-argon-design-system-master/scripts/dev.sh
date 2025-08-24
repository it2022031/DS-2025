#!/usr/bin/env bash
set -euo pipefail

# ------------------------------
# Smart dev runner (npm install only when needed)
# ------------------------------

# Minimal έλεγχοι εργαλείων
command -v node >/dev/null 2>&1 || { echo "❌ Node is not installed"; exit 1; }
command -v npm  >/dev/null 2>&1 || { echo "❌ npm is not installed";  exit 1; }

echo "Node: $(node -v)"
echo "NPM : $(npm -v)"

# -------- Config --------
CACHE_DIR=".cache"
STAMP_FILE="$CACHE_DIR/deps.sha256"
LOCKFILE="package-lock.json"   # αν δεν υπάρχει, χρησιμοποιούμε μόνο package.json
HOST="${HOST:-0.0.0.0}"
PORT="${PORT:-8081}"

# Επιλογές περιβάλλοντος:
#   FORCE_INSTALL=1   -> πάντα τρέχει εγκατάσταση
#   SKIP_INSTALL=1    -> ποτέ δεν κάνει εγκατάσταση (επικίνδυνο αν λείπουν deps)
FORCE_INSTALL="${FORCE_INSTALL:-0}"
SKIP_INSTALL="${SKIP_INSTALL:-0}"

mkdir -p "$CACHE_DIR"

hash_cmd() {
  if command -v sha256sum >/dev/null 2>&1; then
    sha256sum
  elif command -v shasum >/dev/null 2>&1; then
    shasum -a 256
  else
    echo "⚠️  sha256sum/shasum not found; will force install when node_modules is missing."
    cat -
  fi
}

calc_fingerprint() {
  if [ -f "$LOCKFILE" ]; then
    cat package.json "$LOCKFILE" | hash_cmd | awk '{print $1}'
  else
    cat package.json | hash_cmd | awk '{print $1}'
  fi
}

need_install() {
  # Χωρίς node_modules -> χρειάζεται install
  [ -d node_modules ] || return 0

  # Αν δεν μπορούμε να υπολογίσουμε hash (hash_cmd γύρισε κενό), μην βασιστείς σε stamp
  local fp
  fp="$(calc_fingerprint || true)"
  [ -n "$fp" ] || return 1

  # Αν δεν υπάρχει stamp ή άλλαξε -> install
  [ -f "$STAMP_FILE" ] || return 0
  local current
  current="$(cat "$STAMP_FILE" 2>/dev/null || true)"
  [ "$current" != "$fp" ] && return 0

  return 1
}

maybe_install() {
  if [ "$SKIP_INSTALL" = "1" ]; then
    echo "⏭  Skipping dependency install (SKIP_INSTALL=1)."
    return
  fi

  if [ "$FORCE_INSTALL" = "1" ]; then
    echo "🔁 FORCE_INSTALL=1 -> installing deps..."
  elif need_install; then
    echo "📦 Dependencies changed or missing -> installing deps..."
  else
    echo "✅ Dependencies are up-to-date. Skipping npm install."
    return
  fi

  if [ -f "$LOCKFILE" ]; then
    # Προτιμάμε καθαρή, αναπαραγώγιμη εγκατάσταση
    npm ci || npm install --legacy-peer-deps
  else
    npm install --legacy-peer-deps
  fi

  # Αποθήκευση νέου fingerprint (αν έχουμε hash)
  fp="$(calc_fingerprint || true)"
  if [ -n "${fp:-}" ]; then
    echo "$fp" > "$STAMP_FILE"
  fi
}

# Τρέξε εγκατάσταση μόνο αν χρειάζεται
maybe_install

# Εκκίνηση dev server
echo "🚀 Starting dev server on http://${HOST}:${PORT}"
exec npx vue-cli-service serve --host "$HOST" --port "$PORT"

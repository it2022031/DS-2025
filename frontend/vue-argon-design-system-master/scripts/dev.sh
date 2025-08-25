#!/usr/bin/env bash
set -euo pipefail

# ---------------------------------------------
# Smart Vue dev runner (install only when needed)
# και με σιγουριά για devDependencies/Babel
# ---------------------------------------------

# -- Εντοπισμός φακέλου frontend (πρέπει να έχει package.json)
if [[ -f package.json ]]; then
  : # already in frontend root
elif [[ -n "${FRONTEND_DIR:-}" && -f "$FRONTEND_DIR/package.json" ]]; then
  cd "$FRONTEND_DIR"
elif [[ -f "frontend/vue-argon-design-system-master/package.json" ]]; then
  cd "frontend/vue-argon-design-system-master"
elif [[ -f "../frontend/vue-argon-design-system-master/package.json" ]]; then
  cd "../frontend/vue-argon-design-system-master"
else
  echo "❌ Δεν βρέθηκε package.json. Τρέξε το script μέσα στο frontend ή όρισε FRONTEND_DIR=<path>."
  exit 1
fi

# -- Ελάχιστοι έλεγχοι εργαλείων
command -v node >/dev/null 2>&1 || { echo "❌ Node is not installed"; exit 1; }
command -v npm  >/dev/null 2>&1 || { echo "❌ npm is not installed";  exit 1; }

echo "[cwd] $(pwd)"
echo "Node: $(node -v)"
echo "NPM : $(npm -v)"

# -- Ρυθμίσεις
CACHE_DIR=".cache"
STAMP_FILE="$CACHE_DIR/deps.sha256"
LOCKFILE="package-lock.json"
HOST="${HOST:-0.0.0.0}"
PORT="${PORT:-8081}"

# Options:
#   FORCE_INSTALL=1  -> κάνει πάντα install
#   SKIP_INSTALL=1   -> ποτέ install (επικίνδυνο αν λείπουν deps)
FORCE_INSTALL="${FORCE_INSTALL:-0}"
SKIP_INSTALL="${SKIP_INSTALL:-0}"

mkdir -p "$CACHE_DIR"

# Πάντα βάζουμε dev deps, ακόμα κι αν κάποιο περιβάλλον έχει PRODUCTION=true
export NPM_CONFIG_PRODUCTION=false
INCLUDE_DEV="--include=dev"

hash_cmd() {
  if command -v sha256sum >/dev/null 2>&1; then
    sha256sum
  elif command -v shasum >/dev/null 2>&1; then
    shasum -a 256
  else
    echo "⚠️  sha256sum/shasum not found; fallback χωρίς fingerprint."
    cat -
  fi
}

calc_fingerprint() {
  if [[ -f "$LOCKFILE" ]]; then
    cat package.json "$LOCKFILE" | hash_cmd | awk '{print $1}'
  else
    cat package.json | hash_cmd | awk '{print $1}'
  fi
}

need_install() {
  # Λείπει το node_modules;
  [[ -d node_modules ]] || return 0

  # Fingerprint αρχείων deps
  local fp
  fp="$(calc_fingerprint || true)"
  [[ -n "$fp" ]] || return 1

  [[ -f "$STAMP_FILE" ]] || return 0
  local current
  current="$(cat "$STAMP_FILE" 2>/dev/null || true)"
  [[ "$current" != "$fp" ]] && return 0

  return 1
}

ensure_cli() {
  if [[ ! -x node_modules/.bin/vue-cli-service ]]; then
    echo "ℹ️  @vue/cli-service λείπει — εγκατάσταση…"
    npm i -D @vue/cli-service@^5.0.9
  fi
}

ensure_babel() {
  if [[ ! -d node_modules/@vue/cli-plugin-babel ]]; then
    echo "ℹ️  @vue/cli-plugin-babel λείπει — εγκατάσταση…"
    npm i -D @vue/cli-plugin-babel@^5.0.9 @babel/core@^7
  fi
  if [[ ! -f babel.config.js ]]; then
    echo "ℹ️  Δημιουργία babel.config.js"
    cat > babel.config.js <<'JS'
module.exports = {
  presets: ['@vue/cli-plugin-babel/preset']
};
JS
  else
    if ! grep -q "@vue/cli-plugin-babel/preset" babel.config.js; then
      echo "⚠️  Προσθήκη preset στο υπάρχον babel.config.js"
      printf "\nmodule.exports = { presets: ['@vue/cli-plugin-babel/preset'] };\n" >> babel.config.js
    fi
  fi
}

maybe_install() {
  echo "npm config production: $(npm config get production || echo 'n/a')"
  echo "NODE_ENV: ${NODE_ENV:-<unset>} (αγνοείται, βάζουμε dev deps)"

  if [[ "$SKIP_INSTALL" == "1" ]]; then
    echo "⏭  SKIP_INSTALL=1 — παράλειψη εγκατάστασης."
    return
  fi

  if [[ "$FORCE_INSTALL" == "1" ]]; then
    echo "🔁 FORCE_INSTALL=1 -> εγκατάσταση εξαρτήσεων…"
  elif need_install; then
    echo "📦 Εξαρτήσεις άλλαξαν ή λείπουν -> εγκατάσταση…"
  else
    echo "✅ Ενημερωμένες εξαρτήσεις — παράλειψη install."
    ensure_cli
    ensure_babel
    return
  fi

  if [[ -f "$LOCKFILE" ]]; then
    npm ci $INCLUDE_DEV || npm install $INCLUDE_DEV --legacy-peer-deps
  else
    npm install $INCLUDE_DEV --legacy-peer-deps
  fi

  # Εξασφάλιση βασικών dev tooling
  ensure_cli
  ensure_babel

  # Αποθήκευση fingerprint
  local fp
  fp="$(calc_fingerprint || true)"
  if [[ -n "${fp:-}" ]]; then
    echo "$fp" > "$STAMP_FILE"
  fi
}

# -- Run
maybe_install

echo "🌐 Frontend dev server: http://localhost:${PORT}"
exec npx vue-cli-service serve --host "$HOST" --port "$PORT"


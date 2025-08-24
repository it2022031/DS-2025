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

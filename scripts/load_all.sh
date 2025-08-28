#!/usr/bin/env bash
set -euo pipefail

# -------- config --------
DB_NAME="demo_db"
DB_USER="demo_user"

PHOTOS_DIR="${PHOTOS_DIR:-$(pwd)/photos}"
TEST_SQL="${TEST_SQL:-backend/demo/src/main/resources/db/migration/test_dedomena.sql}"
LOAD_PHOTOS_SQL="${LOAD_PHOTOS_SQL:-backend/demo/src/main/resources/db/migration/load_photos.sql}"

# -------- helpers --------
red()   { printf "\033[31m%s\033[0m\n" "$*"; }
green() { printf "\033[32m%s\033[0m\n" "$*"; }
blue()  { printf "\033[34m%s\033[0m\n" "$*"; }
warn()  { printf "⚠️  %s\n" "$*"; }

# find running postgres container (by image)
DB_CONT="$(docker ps --filter 'ancestor=postgres:15' --format '{{.ID}}' | head -n1 || true)"
if [ -z "$DB_CONT" ]; then
  red "❌ Δεν βρέθηκε running container με image postgres:15"
  exit 1
fi

blue "🧩 Χρησιμοποιώ Postgres container: $DB_CONT"
blue "📦 Photos dir: $PHOTOS_DIR"
blue "🗃  SQL test data: $TEST_SQL"
blue "🗃  SQL load photos: $LOAD_PHOTOS_SQL"

# -------- 1) load test data --------
if [ ! -f "$TEST_SQL" ]; then
  red "❌ Δεν βρέθηκε $TEST_SQL"
  exit 1
fi

blue "➡️  Φορτώνω test δεδομένα…"
docker exec -i "$DB_CONT" psql -U "$DB_USER" -d "$DB_NAME" < "$TEST_SQL"
green "✅ Test δεδομένα ΟΚ."

# -------- 2) copy photos & load_photos.sql into container --------
if [ ! -d "$PHOTOS_DIR" ]; then
  red "❌ Δεν βρέθηκε φάκελος φωτογραφιών: $PHOTOS_DIR"
  exit 1
fi

blue "➡️  Αντιγράφω φωτογραφίες στο container…"
docker cp "$PHOTOS_DIR" "$DB_CONT":/photos
green "✅ Αντιγράφηκαν."

if [ ! -f "$LOAD_PHOTOS_SQL" ]; then
  red "❌ Δεν βρέθηκε $LOAD_PHOTOS_SQL"
  exit 1
fi

docker cp "$LOAD_PHOTOS_SQL" "$DB_CONT":/photos/load_photos.sql

# -------- 3) normalize folders inside container --------
docker exec -it "$DB_CONT" bash -lc '
  set -e
  # Normalize property photos folder name (remove trailing space, fix typos)
  if [ -d "/photos/property_photos " ]; then mv "/photos/property_photos " "/photos/property_photos"; fi
  if [ -d "/photos/propertie_photoes" ]; then mv "/photos/propertie_photoes" "/photos/property_photos"; fi
  if [ -d "/photos/property_photos" ]; then echo "✅ property_photos OK"; else echo "❌ λείπει /photos/property_photos"; fi

  # Normalize avatar folder (old name -> new)
  if [ -d "/photos/avatars_photoes" ]; then mv "/photos/avatars_photoes" "/photos/avatar_photos"; fi
  if [ -d "/photos/avatar_photos" ]; then echo "✅ avatar_photos OK"; else echo "❌ λείπει /photos/avatar_photos"; fi

  ls -la /photos
'

# -------- 4) run load_photos.sql --------
blue "➡️  Τρέχω load_photos.sql…"
docker exec -i "$DB_CONT" psql -U "$DB_USER" -d "$DB_NAME" -f /photos/load_photos.sql || warn "Υπήρξαν errors στο load_photos.sql — έλεγξε paths/ονόματα"

green "✅ Φωτογραφίες φορτώθηκαν."

# -------- 5) sanity checks --------
blue "ℹ️  Έλεγχος property photos:"
docker exec -it "$DB_CONT" psql -U "$DB_USER" -d "$DB_NAME" -c \
"SELECT id, property_id, filename, octet_length(image) AS bytes FROM property_photos ORDER BY id LIMIT 20;"

blue "ℹ️  Έλεγχος avatar photos:"
docker exec -it "$DB_CONT" psql -U "$DB_USER" -d "$DB_NAME" -c \
"SELECT id, username, profile_picture_filename, octet_length(profile_picture) AS bytes FROM users ORDER BY id;"


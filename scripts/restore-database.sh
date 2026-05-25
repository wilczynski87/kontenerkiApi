#!/usr/bin/env bash
# Importuje zrzut PostgreSQL do świeżej bazy db1 (docker compose: serwis db).
# Użycie: ./scripts/restore-database.sh /ścieżka/do/kontenerki-db1-YYYYMMDD.sql
set -euo pipefail

if [[ $# -lt 1 ]]; then
  echo "Usage: $0 <dump.sql>" >&2
  exit 1
fi

DUMP="$1"
if [[ ! -f "$DUMP" ]]; then
  echo "File not found: $DUMP" >&2
  exit 1
fi

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

DB_PORT="${DB_PORT:-5431}"
DB_USER="${DB_USER:-admin_user}"
DB_PASSWORD="${DB_PASSWORD:-postgres}"
DB_NAME="${DB_NAME:-db1}"

export PGPASSWORD="$DB_PASSWORD"

if ! pg_isready -h localhost -p "$DB_PORT" -U "$DB_USER" >/dev/null 2>&1; then
  echo "PostgreSQL not reachable on localhost:$DB_PORT — starting docker compose db..."
  if docker compose up -d db 2>/dev/null; then
    :
  elif sudo docker compose up -d db 2>/dev/null; then
    :
  else
    echo "Could not start db service. Start PostgreSQL manually or fix Docker permissions." >&2
    exit 1
  fi
fi

echo "Waiting for PostgreSQL..."
for _ in $(seq 1 30); do
  if pg_isready -h localhost -p "$DB_PORT" -U "$DB_USER" >/dev/null 2>&1; then
    break
  fi
  sleep 1
done

if ! pg_isready -h localhost -p "$DB_PORT" -U "$DB_USER" >/dev/null 2>&1; then
  echo "PostgreSQL not ready on localhost:$DB_PORT (set DB_PORT to match docker compose mapping, often 5431 or 5432)." >&2
  exit 1
fi

echo "Recreating database $DB_NAME..."
psql -h localhost -p "$DB_PORT" -U "$DB_USER" -d postgres -v ON_ERROR_STOP=1 <<SQL
SELECT pg_terminate_backend(pid) FROM pg_stat_activity WHERE datname = '$DB_NAME' AND pid <> pg_backend_pid();
DROP DATABASE IF EXISTS "$DB_NAME";
CREATE DATABASE "$DB_NAME";
SQL

echo "Importing dump..."
psql -h localhost -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -v ON_ERROR_STOP=1 -f "$DUMP"

echo "Applying optional post-restore migrations..."
psql -h localhost -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -v ON_ERROR_STOP=1 -f "$ROOT/scripts/post-restore-migrations.sql"

echo "Done. Start API with: DB_HOST=localhost DB_PORT=$DB_PORT ./gradlew run"
echo "If startup still fails on schema migration, set DB_AUTO_MIGRATE=false in .env"

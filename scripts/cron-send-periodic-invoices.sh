#!/usr/bin/env bash
# Sends periodic invoices for all active clients (1st-of-month cron on VPS).
# Requires INTERNAL_API_KEY (same as API .env). Optional API_BASE_URL (default http://127.0.0.1:8100).
#
# Crontab example (1st day of month at 06:00):
#   0 6 1 * * /opt/app/scripts/cron-send-periodic-invoices.sh >> /var/log/kontenerki-periodic-invoices.log 2>&1
set -euo pipefail

API_BASE_URL="${API_BASE_URL:-http://127.0.0.1:8100}"
INTERNAL_API_KEY="${INTERNAL_API_KEY:-}"

if [[ -z "$INTERNAL_API_KEY" ]]; then
  echo "$(date -Iseconds) ERROR: INTERNAL_API_KEY is not set" >&2
  exit 1
fi

URL="${API_BASE_URL%/}/internal/invoice/sendInvoices/forAll"
echo "$(date -Iseconds) POST $URL"

HTTP_CODE=0
BODY_FILE="$(mktemp)"
trap 'rm -f "$BODY_FILE"' EXIT

set +e
HTTP_CODE=$(curl -sS -o "$BODY_FILE" -w "%{http_code}" \
  -X POST \
  -H "X-Internal-Key: ${INTERNAL_API_KEY}" \
  -H "Accept: application/json" \
  "$URL")
CURL_EXIT=$?
set -e

BODY="$(cat "$BODY_FILE" 2>/dev/null || true)"
echo "$(date -Iseconds) HTTP $HTTP_CODE"
echo "$BODY"

if [[ $CURL_EXIT -ne 0 ]]; then
  echo "$(date -Iseconds) ERROR: curl failed with exit $CURL_EXIT" >&2
  exit "$CURL_EXIT"
fi

if [[ "$HTTP_CODE" -eq 401 ]] || [[ "$HTTP_CODE" -ge 500 ]]; then
  echo "$(date -Iseconds) ERROR: unexpected HTTP status $HTTP_CODE" >&2
  exit 1
fi

if [[ "$HTTP_CODE" -lt 200 ]] || [[ "$HTTP_CODE" -ge 300 ]]; then
  echo "$(date -Iseconds) ERROR: unexpected HTTP status $HTTP_CODE" >&2
  exit 1
fi

exit 0

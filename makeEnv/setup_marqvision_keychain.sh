#!/usr/bin/env bash
set -euo pipefail

SERVICE="marqvision.login"
ACCOUNT="marqvision"

echo "== MarqVision credentials → macOS Keychain =="
read -rp "MARQVISION_EMAIL: " EMAIL
read -rsp "MARQVISION_PASSWORD: " PASSWORD; echo

# JSON(평문)으로 묶어서 Keychain에 저장(업데이트 포함)
escape_json() { printf '%s' "$1" | sed 's/\\/\\\\/g; s/"/\\"/g'; }
JSON=$(printf '{"email":"%s","password":"%s"}' \
  "$(escape_json "$EMAIL")" "$(escape_json "$PASSWORD")")

security add-generic-password -a "$ACCOUNT" -s "$SERVICE" -w "$JSON" -U >/dev/null

echo "Saved to Keychain: account=$ACCOUNT, service=$SERVICE"
echo "Tip) Test read:"
echo "security find-generic-password -a $ACCOUNT -s $SERVICE -w"

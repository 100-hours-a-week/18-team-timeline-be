#!/bin/bash

STATUS="$1"
TITLE="$2"
MESSAGE="$3"
WEBHOOK_URL="$4"

# ✔️ 먼저 color를 조건문으로 따로 설정
if [ "$STATUS" = "success" ]; then
  COLOR=3066993   # green
else
  COLOR=15158332  # red
fi

# ✔️ 그 후에 payload에 COLOR 삽입
PAYLOAD="{
  \"username\": \"GitHub Actions\",
  \"embeds\": [{
    \"title\": \"$TITLE\",
    \"description\": \"$MESSAGE\",
    \"color\": $COLOR
  }]
}"

curl -H "Content-Type: application/json" -X POST -d "$PAYLOAD" "$WEBHOOK_URL"

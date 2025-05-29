#!/bin/bash

STATUS=$1
TITLE=$2
MESSAGE=$3
WEBHOOK_URL=$4

PAYLOAD="{
  \"username\": \"GitHub Actions\",
  \"embeds\": [{
    \"title\": \"$TITLE\",
    \"description\": \"$MESSAGE\",
    \"color\": $(if [ "$STATUS" = "success" ]; then echo 3066993; else echo 15158332; fi)
  }]
}"

curl -H "Content-Type: application/json" -X POST -d "$PAYLOAD" "$WEBHOOK_URL"

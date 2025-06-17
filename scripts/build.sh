#!/bin/bash
set -e

ECR_REPO=$1
TAG=$2

docker build \
  --build-arg DB_URL=$DB_URL \
  --build-arg DB_USERNAME=$DB_USERNAME \
  --build-arg DB_PASSWORD=$DB_PASSWORD \
  --build-arg KAKAO_CLIENT_ID=$KAKAO_CLIENT_ID \
  --build-arg KAKAO_REDIRECT_URI=$KAKAO_REDIRECT_URI \
  --build-arg JWT_SECRET_CODE=$JWT_SECRET_CODE \
  --build-arg FE_BASE_URL=$FE_BASE_URL \
  --build-arg BE_BASE_URL=$BE_BASE_URL \
  --build-arg AI_BASE_URL=$AI_BASE_URL \
  --build-arg EC2_PUBLIC_URL_1=$EC2_PUBLIC_URL_1 \
  --build-arg EC2_PUBLIC_URL_2=$EC2_PUBLIC_URL_2 \
  --build-arg EC2_PUBLIC_URL_3=$EC2_PUBLIC_URL_3 \
  --build-arg OTEL_EXPORTER_OTLP_ENDPOINT=$OTEL_EXPORTER_OTLP_ENDPOINT \
  --build-arg ECR_URI_DEV=$ECR_URI_DEV \
  -t $ECR_REPO:$TAG .
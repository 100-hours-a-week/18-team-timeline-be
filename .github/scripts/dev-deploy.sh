#!/bin/bash

TAG=$1
BACKEND_EC2="ec2-user@${BACKEND_EC2_IP}"  # <-- 환경변수로 전달받음
ECR_URI="${ECR_URI_DEV}"                 # <-- 환경변수로 전달받음

echo "[1] 백엔드 EC2로 SSH 접속 후 컨테이너 배포 시작"

ssh -o StrictHostKeyChecking=no $BACKEND_EC2 << EOF
  echo "[백엔드 EC2] 최신 컨테이너 Pull"
  docker pull $ECR_URI:$TAG

  echo "[백엔드 EC2] 기존 컨테이너 중지 및 제거"
  docker stop backend || true
  docker rm backend || true

  echo "[백엔드 EC2] 새로운 컨테이너 실행"
  docker run -d --name backend -p 8080:8080 \
    --restart unless-stopped \
    $ECR_URI:$TAG
EOF

echo "✅ 배포 완료 (TAG: $TAG)"
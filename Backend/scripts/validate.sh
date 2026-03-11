#!/bin/bash 
#배포 완료 후 서버 정상 여부 체크
set -e

echo "Validating backend service..."

sleep 15

curl -f http://localhost:8080/api/health

echo "Backend deployment successful"
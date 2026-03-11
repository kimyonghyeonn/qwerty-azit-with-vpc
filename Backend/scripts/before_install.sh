#!/bin/bash 
#배포 전 디렉토리 생성 => 기존 jar 백업
set -e

echo "Preparing deployment directory..."

mkdir -p /srv/app/backend
mkdir -p /srv/app/backup

if [ -f /srv/app/backend/app.jar ]; then
    echo "Backing up existing jar..."
    TIMESTAMP=$(date +%Y%m%d_%H%M%S)
    cp /srv/app/backend/app.jar /srv/app/backup/app-$TIMESTAMP.jar
fi
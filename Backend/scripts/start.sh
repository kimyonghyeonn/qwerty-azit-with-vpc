#!/bin/bash 
#배포된 jar로 backend 실행
set -e

echo "Starting backend service..."

sudo systemctl daemon-reload
sudo systemctl start backend
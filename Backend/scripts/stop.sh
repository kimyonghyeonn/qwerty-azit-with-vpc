#!/bin/bash 
#배포 전 기존 서버 중지
set -e

echo "Stopping backend service..."

if sudo systemctl is-active --quiet backend; then
    sudo systemctl stop backend
    echo "Backend stopped"
else
    echo "Backend already stopped"
fi
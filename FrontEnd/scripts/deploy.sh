#!/bin/bash
set -e

echo "frontend deploy start"

chown -R ec2-user:ec2-user /var/www/frontend
chmod -R 755 /var/www/frontend

nginx -t
systemctl reload nginx

echo "frontend deploy complete"
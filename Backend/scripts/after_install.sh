#!/bin/bash
set -e

chown ec2-user:ec2-user /srv/app/backend/app.jar
chmod 644 /srv/app/backend/app.jar
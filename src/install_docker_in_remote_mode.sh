#!/bin/bash

set -x # Echo on
set -e # Stop on the first error

# Update teh system clock from hardware. Useful is running VM whose
# system clock is lagging behind.
hwclock --hctosys

export DEBIAN_FRONTEND=noninteractive

DOCKERD_HOME="/etc/systemd/system/docker.service.d"
CONFIGURATION_FILE="startup_options.conf"

mkdir -p "${DOCKERD_HOME}"

# Install Docker
curl -sSL https://get.docker.com | sh

# Configure Docker to accept remote connections
cat >>  "${DOCKERD_HOME}/${CONFIGURATION_FILE}" <<EOF
[Service]
ExecStart=
ExecStart=/usr/bin/dockerd -H tcp://0.0.0.0:2376 -H unix:///var/run/docker.sock
EOF

systemctl daemon-reload
systemctl restart docker.service

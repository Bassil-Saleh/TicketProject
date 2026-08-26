#!/usr/bin/env bash
#
# make-certs.sh
#
# Generates a locally-trusted TLS certificate for the app hostname using
# mkcert, and installs mkcert's local root CA into THIS machine's trust store.
#
# To make other devices (e.g. your phone) trust the certificate, install the
# root CA printed at the end of this script onto each device.

set -euo pipefail

cd "$(dirname "$0")/.."

# Read SITE_HOST from .env if present, otherwise use the default.
SITE_HOST="ticketproject.local"
if [ -f .env ]; then
    SITE_HOST="$(grep -E '^SITE_HOST=' .env | tail -n 1 | cut -d '=' -f 2- || true)"
    SITE_HOST="${SITE_HOST:-ticketproject.local}"
fi

if ! command -v mkcert >/dev/null 2>&1; then
    echo "ERROR: mkcert is not installed." >&2
    echo "Install it first, e.g.:" >&2
    echo "  Ubuntu/Debian: sudo apt install -y mkcert libnss3-tools" >&2
    echo "  macOS:         brew install mkcert" >&2
    exit 1
fi

mkdir -p certs

echo "Installing mkcert's local root CA into this machine's trust store..."
mkcert -install

echo "Generating a certificate for ${SITE_HOST} ..."
mkcert \
    -cert-file "certs/${SITE_HOST}.pem" \
    -key-file "certs/${SITE_HOST}-key.pem" \
    "${SITE_HOST}"

echo
echo "Certificate written to certs/${SITE_HOST}.pem"
echo "Private key written to certs/${SITE_HOST}-key.pem"
echo
echo "To trust this certificate on OTHER devices (phones, laptops), install the"
echo "root CA located at: $(mkcert -CAROOT)/rootCA.pem"
echo "  Android: Settings -> Security -> Install certificate (choose user CA)."
echo "  iOS:     Install the profile, then enable full trust under"
echo "           Settings -> General -> About -> Certificate Trust Settings."

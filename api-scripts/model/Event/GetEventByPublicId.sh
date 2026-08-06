#!/bin/sh

# Defaults
token=""
publicId=""

# Parse named parameters
while [ $# -gt 0 ]; do
    case "$1" in
        --token)    token="$2";   shift 2 ;;
        --publicId) publicId="$2" shift 2 ;;
        *)          shift ;;
    esac
done

# Validate mandatory parameters
if [ -z "$token" ] || [ -z "$publicId" ]; then
    echo "Usage: $0 --token <value> --publicId <value>"
    exit 1
fi

authHeader="Authorization: Bearer $token"
url="http://localhost:8080/api/v1/events/${publicId}"

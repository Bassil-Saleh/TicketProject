#!/bin/sh

# Defaults
publicId=""

# Parse named parameters
while [ $# -gt 0 ]; do
    case "$1" in
        --publicId) publicId="$2" shift 2 ;;
        *)          shift ;;
    esac
done

# Validate mandatory parameters
if [ -z "$publicId" ]; then
    echo "Usage: $0 --publicId <value>"
    exit 1
fi

url="http://localhost:8080/api/v1/events/${publicId}"

curl -X GET "$url"
echo
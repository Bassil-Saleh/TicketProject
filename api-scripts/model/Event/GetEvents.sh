#!/bin/sh

# Defaults
token=""
count=""

# Parse named parameters
while [ $# -gt 0 ]; do
    case "$1" in
        --token) token="$2" shift 2 ;;
        --count) count="$2" shift 2 ;;
        *)       shift ;;
    esac
done

# Validate mandatory parameters
if [ -z "$token" ] || [ -z "$count" ]; then
    echo "Usage: $0 --token <value> --count <value>"
    exit 1
fi

authHeader="Authorization: Bearer $token"

curl -X GET -H "$authHeader" "http://localhost:8080/api/v1/events?count=${count}"
echo
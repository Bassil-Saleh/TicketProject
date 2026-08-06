#!/bin/sh

# Defaults
token=""
publicId=""
description=""

# Parse named parameters
while [ $# -gt 0 ]; do
	case "$1" in
		--token)          token="$2";          shift 2 ;;
		--publicId)       publicId="$2";       shift 2 ;;
        --description)    description="$2"     shift 2 ;;
		*)                shift ;;
	esac
done

# Validate mandatory parameters
if [ -z "$token" ] || [ -z "$publicId" ] || [ -z "$description" ]; then
    echo "Usage: $0 --token <value> --publicId <value> --description <value>"
    exit 1
fi

authHeader="Authorization: Bearer $token"
contentTypeHeader="Content-Type: application/json"
json="{\"publicId\": \"$publicId\""
json="${json}, \"description\": \"$description\""
json="${json}}"

curl -X PATCH -H "$authHeader" -H "$contentTypeHeader" -d "$json" http://localhost:8080/api/v1/events/description
echo
#!/bin/sh

# Defaults
token=""
publicId=""
maxAttendees=""

# Parse named parameters
while [ $# -gt 0 ]; do
	case "$1" in
		--token)          token="$2";          shift 2 ;;
		--max-attendees)  maxAttendees="$2";   shift 2 ;;
		--public-id)      publicId="$2";       shift 2 ;;
		*)                shift ;;
	esac
done

# Validate mandatory parameters
if [ -z "$token" ] || [ -z "$publicId" ] || [ -z "$maxAttendees" ]; then
	echo "Usage: $0 --token <value> --public-id <value> --max-attendees <value>"
	exit 1
fi

authHeader="Authorization: Bearer $token"
contentTypeHeader="Content-Type: application/json"
json="{\"publicId\": \"$publicId\""
json="${json}, \"maxAttendees\": $maxAttendees"
json="${json}}"

curl -X PATCH -H "$authHeader" -H "$contentTypeHeader" -d "$json" http://localhost:8080/api/v1/events/change-to-public
echo
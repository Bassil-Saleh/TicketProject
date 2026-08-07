#!/bin/sh

# Defaults
token=""
publicId=""
startDateTime=""
endDateTime=""

# Parse named parameters
while [ $# -gt 0 ]; do
	case "$1" in
		--token)          token="$2";          shift 2 ;;
		--publicId)       publicId="$2";       shift 2 ;;
		--startDateTime)  startDateTime="$2";  shift 2 ;;
		--endDateTime)    endDateTime="$2";    shift 2 ;;
		*)                shift ;;
	esac
done

# Validate mandatory parameters
if [ -z "$token" ] || [ -z "$publicId" ] || [ -z "$startDateTime" ] || [ -z "$endDateTime" ]; then
    echo "Usage: $0 --token <value> --publicId <value> --startDateTime <value> --endDateTime <value>"
    exit 1
fi

authHeader="Authorization: Bearer $token"
contentTypeHeader="Content-Type: application/json"
json="{\"publicId\": \"$publicId\""
json="${json}, \"startDateTime\": \"$startDateTime\""
json="${json}, \"endDateTime\": \"$endDateTime\""
json="${json}}"

curl -X PATCH -H "$authHeader" -H "$contentTypeHeader" -d "$json" http://localhost:8080/api/v1/events/dates
echo
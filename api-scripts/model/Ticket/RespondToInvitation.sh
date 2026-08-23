#!/bin/sh

# Defaults
publicToken=""
invitationResponse=""
message=""

# Parse named parameters
while [ $# -gt 0 ]; do
	case "$1" in
		--public-token)        publicToken="$2";        shift 2 ;;
        --invitation-response) invitationResponse="$2"; shift 2 ;;
		--message)             message="$2";            shift 2 ;;
		*)                     shift ;;
	esac
done

# Validate mandatory parameters
if [ -z "$publicToken" ] || [ -z "$invitationResponse" ]; then
    echo "Usage: $0 --public-token <value> --invitation-response <value> [--message <value>]"
    exit 1
fi

contentTypeHeader="Content-Type: application/json"
json="{ \"publicToken\": \"$publicToken\""
json="${json}, \"invitationResponse\": \"$invitationResponse\""
if [ -n "$message" ]; then
	json="${json}, \"message\": \"$message\""
fi
json="${json}}"

curl -X PATCH -H "$contentTypeHeader" -d "$json" http://localhost:8080/api/v1/tickets/invitation
echo
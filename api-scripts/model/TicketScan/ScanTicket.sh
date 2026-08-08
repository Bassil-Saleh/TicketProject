#!/bin/sh

# Defaults
jwtToken=""
ticketToken=""

# Parse named parameters
while [ $# -gt 0 ]; do
	case "$1" in
		--jwt-token)    jwtToken="$2";    shift 2 ;;
		--ticket-token) ticketToken="$2"; shift 2 ;;
		*)              shift ;;
	esac
done

# Validate mandatory parameters
if [ -z "$jwtToken" ] || [ -z "$ticketToken" ]; then
    echo "Usage: $0 --jwt-token <value> --ticket-token <value>"
    exit 1
fi

authHeader="Authorization: Bearer $jwtToken"
contentTypeHeader="Content-Type: application/json"
json="{ \"publicToken\": \"$ticketToken\" }"

curl -X POST -H "$authHeader" -H "$contentTypeHeader" -d "$json" http://localhost:8080/api/v1/ticket-scans
echo
#!/bin/sh

# Defaults
token=""

# Parse named parameters
while [ $# -gt 0 ]; do
	case "$1" in
		--token) token="$2"; shift 2 ;;
		*)       shift ;;
	esac
done

# Validate mandatory parameters
if [ -z "$token" ]; then
	echo "Usage: $0 --token <value>"
	exit 1
fi

header="Authorization: Bearer $token"

curl -X GET -H "$header" http://localhost:8080/api/v1/ticket-scans
echo
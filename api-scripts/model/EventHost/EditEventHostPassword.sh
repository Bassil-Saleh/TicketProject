#!/bin/sh

# Defaults
token=""
password=""

# Parse named parameters
while [ $# -gt 0 ]; do
	case "$1" in
		--token)    token="$2";    shift 2 ;;
		--password) password="$2"; shift 2 ;;
		*)          shift ;;
	esac
done

# Validate mandatory parameters
if [ -z "$token" ] || [ -z "$password" ]; then
	echo "Usage: $0 --token <value> --password <value>"
	exit 1
fi

authHeader="Authorization: Bearer $token"
contentTypeHeader="Content-Type: application/json"
json="{\"password\": \"$password\"}"

curl -X PATCH -H "$authHeader" -H "$contentTypeHeader" -d "$json" http://localhost:8080/api/v1/event-hosts/password
echo
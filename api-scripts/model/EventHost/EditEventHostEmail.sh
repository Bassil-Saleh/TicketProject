#!/bin/sh

# Defaults
token=""
email=""

# Parse named parameters
while [ $# -gt 0 ]; do
	case "$1" in
		--token) token="$2"; shift 2 ;;
		--email) email="$2"; shift 2 ;;
		*)       shift ;;
	esac
done

# Validate mandatory parameters
if [ -z "$token" ] || [ -z "$email" ]; then
	echo "Usage: $0 --token <value> --email <value>"
	exit 1
fi

authHeader="Authorization: Bearer $token"
contentTypeHeader="Content-Type: application/json"
json="{\"email\": \"$email\"}"

curl -X PATCH -H "$authHeader" -H "$contentTypeHeader" -d "$json" http://localhost:8080/api/v1/event-hosts/email
echo
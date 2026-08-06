#!/bin/sh

# Defaults
passwordResetToken=""
password=""

# Parse named parameters
while [ $# -gt 0 ]; do
	case "$1" in
		--token)    passwordResetToken="$2"; shift 2 ;;
		--password) password="$2";           shift 2 ;;
		*)          shift ;;
	esac
done

# Validate mandatory parameters
if [ -z "$passwordResetToken" ] || [ -z "$password" ]; then
	echo "Usage: $0 --token <value> --password <value>"
	exit 1
fi

contentTypeHeader="Content-Type: application/json"
json="{\"passwordResetToken\": \"$passwordResetToken\""
json="${json}, \"password\": \"$password\"}"

curl -X PATCH -H "$contentTypeHeader" -d "$json" http://localhost:8080/api/v1/password-reset-tokens
echo
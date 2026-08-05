#!/bin/sh

if [ -z "$1" ]; then
	echo "Need to supply a password reset token"
	exit 1;
fi

if [ -z "$2" ]; then
	echo "Need to supply the new password"
	exit 1;
fi

contentTypeHeader="Content-Type: application/json"
json="{\"passwordResetToken\": \"$1\","
json="${json} \"password\": \"$2\"}"

curl -X PATCH -H "$contentTypeHeader" -d "$json" http://localhost:8080/api/v1/password-reset-tokens

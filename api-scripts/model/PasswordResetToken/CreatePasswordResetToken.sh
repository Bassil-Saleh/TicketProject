#!/bin/sh

if [ -z "$1" ]; then
	echo "Need to supply an email address"
	exit 1;
fi

json="{\"email\": \"$1\"}"

curl --json "$json" http://localhost:8080/api/v1/password-reset-tokens

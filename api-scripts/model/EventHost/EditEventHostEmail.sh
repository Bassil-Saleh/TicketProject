#!/bin/sh

if [ -z "$1" ]; then
	echo "Need to supply a JSON Web Token"
	exit 1;
fi

if [ -z "$2" ]; then
	echo "Need to supply the new email address"
	exit 1;
fi

authHeader="Authorization: Bearer $1"
contentTypeHeader="Content-Type: application/json"
json="{\"email\": \"$2\"}"

curl -X PATCH -H "$authHeader" -H "$contentTypeHeader" -d "$json" http://localhost:8080/api/v1/event-hosts/email

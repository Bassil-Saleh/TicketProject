#!/bin/sh

if [ -z "$1" ]; then
	echo "Need to supply a JSON Web Token"
	exit 1;
fi

if [ -z "$2" ]; then
	echo "Need to supply the new first name"
	exit 1;
fi

if [ -z "$3" ]; then
	echo "Need to supply the new middle name"
	exit 1;
fi

if [ -z "$4" ]; then
	echo "Need to supply the new last name"
	exit 1;
fi

authHeader="Authorization: Bearer $1"
contentTypeHeader="Content-Type: application/json"
json="{\"firstName\": \"$2\","
json="${json} \"middleName\": \"$3\","
json="${json} \"lastName\": \"$4\"}"

curl -X PATCH -H "$authHeader" -H "$contentTypeHeader" -d "$json" http://localhost:8080/api/v1/event-hosts/name

#!/bin/sh

if [ -z "$1" ]; then
	echo "Need to supply a JSON Web Token"
	exit 1;
fi

header="Authorization: Bearer $1"

curl -X GET -H "$header" http://localhost:8080/api/v1/event-hosts/profile

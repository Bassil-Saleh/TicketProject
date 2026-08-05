#!/bin/sh

if [ -z "$1" ]; then
	echo "Need to supply a JSON Web Token"
	exit 1;
fi

header="Authorization: Bearer $1"
#echo $header

curl -X PATCH -H "$header" http://localhost:8080/api/v1/sessions/logout-all-devices

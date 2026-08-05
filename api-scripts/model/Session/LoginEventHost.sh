#!/bin/sh

if [ -z "$1" ]; then
	echo "Need to supply an email address"
	exit 1;
fi

if [ -z "$2" ]; then
	echo "Need to supply a password"
	exit 1;
fi

json="{\"email\": \"$1\", \"password\": \"$2\"}"
#echo $json

curl --json "$json" http://localhost:8080/api/v1/sessions/login

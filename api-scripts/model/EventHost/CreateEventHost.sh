#!/bin/sh

if [ -z "$1" ]; then
	echo "Need to supply a first name"
	exit 1;
fi

if [ -z "$2" ]; then
	echo "Need to supply a middle name"
	exit 1;
fi

if [ -z "$3" ]; then
	echo "Need to supply a last name"
	exit 1;
fi

if [ -z "$4" ]; then
	echo "Need to supply an email address"
	exit 1;
fi

if [ -z "$5" ]; then
	echo "Need to supply a password"
	exit 1;
fi

if [ -z "$6" ]; then
	echo "Need to supply a date of birth"
	exit 1;
fi

json="{\"firstName\": \"$1\","
json="${json} \"middleName\": \"$2\","
json="${json} \"lastName\": \"$3\","
json="${json} \"email\":\"$4\","
json="${json} \"password\":\"$5\","
json="${json} \"dateOfBirth\":\"$6\"}"

#echo "$json"

curl --json "$json" http://localhost:8080/api/v1/event-hosts

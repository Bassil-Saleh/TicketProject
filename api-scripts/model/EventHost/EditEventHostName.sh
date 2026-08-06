#!/bin/sh

# Defaults
token=""
firstName=""
middleName=""
lastName=""

# Parse named parameters
while [ $# -gt 0 ]; do
	case "$1" in
		--token)       token="$2";       shift 2 ;;
		--first-name)  firstName="$2";   shift 2 ;;
		--middle-name) middleName="$2";  shift 2 ;;
		--last-name)   lastName="$2";    shift 2 ;;
		*)             shift ;;
	esac
done

# Validate mandatory parameters
if [ -z "$token" ] || [ -z "$firstName" ] || [ -z "$lastName" ]; then
	echo "Usage: $0 --token <value> --first-name <value> --last-name <value> [--middle-name <value>]"
	exit 1
fi

authHeader="Authorization: Bearer $token"
contentTypeHeader="Content-Type: application/json"
json="{\"firstName\": \"$firstName\""
if [ -n "$middleName" ]; then
	json="${json}, \"middleName\": \"$middleName\""
fi
json="${json}, \"lastName\": \"$lastName\"}"

curl -X PATCH -H "$authHeader" -H "$contentTypeHeader" -d "$json" http://localhost:8080/api/v1/event-hosts/name
echo
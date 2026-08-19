#!/bin/sh

# Defaults
token=""
publicId=""
firstName=""
middleName=""
lastName=""
email=""

# Parse named parameters
while [ $# -gt 0 ]; do
	case "$1" in
        --token)           token="$2";         shift 2 ;;
		--public-id)       publicId="$2";      shift 2 ;;
		--first-name)      firstName="$2";     shift 2 ;;
		--middle-name)     middleName="$2";    shift 2 ;;
		--last-name)       lastName="$2";      shift 2 ;;
		--email)           email="$2";         shift 2 ;;
		*)                 shift ;;
	esac
done

# Validate mandatory parameters
if [ -z "$token" ] || [ -z "$publicId" ] || [ -z "$firstName" ] || [ -z "$lastName" ] || [ -z "$email" ]; then
	echo "Usage: $0 --token <value> --public-id <value> --first-name <value> --last-name <value> --email <value> [--middle-name <value>]"
	exit 1
fi

authHeader="Authorization: Bearer $token"
contentTypeHeader="Content-Type: application/json"
# Build JSON (only include middleName if provided)
json="{\"firstName\": \"$firstName\""
if [ -n "$middleName" ]; then
	json="${json}, \"middleName\": \"$middleName\""
fi
json="${json}, \"lastName\": \"$lastName\""
json="${json}, \"email\": \"$email\""
json="${json}, \"publicId\": \"$publicId\""
json="${json}}"

curl -X POST -H "$authHeader" -H "$contentTypeHeader" -d "$json" http://localhost:8080/api/v1/attendees/invitation
echo
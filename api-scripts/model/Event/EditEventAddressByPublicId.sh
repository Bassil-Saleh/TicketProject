#!/bin/sh

# Defaults
token=""
publicId=""
addressLine1=""
addressLine2=""
city=""
state=""
postalCode=""
country=""
latitude=""
longitude=""

# Parse named parameters
while [ $# -gt 0 ]; do
	case "$1" in
		--token)          token="$2";         shift 2 ;;
		--public-id)      publicId="$2";      shift 2 ;;
		--address-line-1) addressLine1="$2";  shift 2 ;;
		--address-line-2) addressLine2="$2";  shift 2 ;;
		--city)           city="$2";          shift 2 ;;
		--state)          state="$2";         shift 2 ;;
		--postal-code)    postalCode="$2";    shift 2 ;;
		--country)        country="$2";       shift 2 ;;
		--latitude)       latitude="$2";      shift 2 ;;
		--longitude)      longitude="$2";     shift 2 ;;
		*)                shift ;;
	esac
done

# Validate mandatory parameters
if [ -z "$token" ] || [ -z "$publicId" ] || [ -z "$addressLine1" ] || [ -z "$city" ] || [ -z "$state" ] || [ -z "$postalCode" ] || [ -z "$country" ]; then
	echo "Usage: $0 --token <value> --public-id <value> --address-line-1 <value> --city <value> --state <value> --postal-code <value> --country <value> [--address-line-2 <value>] [--latitude <value>] [--longitude <value>]"
	exit 1
fi

authHeader="Authorization: Bearer $token"
contentTypeHeader="Content-Type: application/json"
json="{\"publicId\": \"$publicId\""
json="${json}, \"addressLine1\": \"$addressLine1\""
if [ -n "$addressLine2" ]; then
	json="${json}, \"addressLine2\": \"$addressLine2\""
fi
json="${json}, \"city\": \"$city\""
json="${json}, \"state\": \"$state\""
json="${json}, \"postalCode\": \"$postalCode\""
json="${json}, \"country\": \"$country\""
if [ -n "$latitude" ]; then
	json="${json}, \"latitude\": $latitude"
fi
if [ -n "$longitude" ]; then
	json="${json}, \"longitude\": $longitude"
fi
json="${json}}"

curl -X PATCH -H "$authHeader" -H "$contentTypeHeader" -d "$json" http://localhost:8080/api/v1/events/address
echo
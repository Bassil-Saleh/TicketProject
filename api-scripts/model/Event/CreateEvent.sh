#!/bin/sh

# Defaults
token=""
name=""
description=""
startDateTime=""
endDateTime=""
eventType=""
maxAttendees=""
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
		--token)          token="$2";          shift 2 ;;
		--name)           name="$2";           shift 2 ;;
		--description)    description="$2";    shift 2 ;;
		--start-date)     startDateTime="$2";  shift 2 ;;
		--end-date)       endDateTime="$2";    shift 2 ;;
		--event-type)     eventType="$2";      shift 2 ;;
		--max-attendees)  maxAttendees="$2";   shift 2 ;;
		--address-line1)  addressLine1="$2";   shift 2 ;;
		--address-line2)  addressLine2="$2";   shift 2 ;;
		--city)           city="$2";           shift 2 ;;
		--state)          state="$2";          shift 2 ;;
		--postal-code)    postalCode="$2";     shift 2 ;;
		--country)        country="$2";        shift 2 ;;
		--latitude)       latitude="$2";       shift 2 ;;
		--longitude)      longitude="$2";      shift 2 ;;
		*)                shift ;;
	esac
done

# Validate mandatory parameters
if [ -z "$token" ] || [ -z "$name" ] || [ -z "$description" ] || [ -z "$startDateTime" ] || [ -z "$endDateTime" ] || [ -z "$eventType" ] || [ -z "$maxAttendees" ] || [ -z "$addressLine1" ] || [ -z "$city" ] || [ -z "$state" ] || [ -z "$postalCode" ] || [ -z "$country" ]; then
	echo "Usage: $0 --token <value> --name <value> --description <value> --start-date <value> --end-date <value> --event-type <value> --max-attendees <value> --address-line1 <value> --city <value> --state <value> --postal-code <value> --country <value> [--address-line2 <value>] [--latitude <value>] [--longitude <value>]"
	exit 1
fi

authHeader="Authorization: Bearer $token"
contentTypeHeader="Content-Type: application/json"
json="{\"name\": \"$name\""
json="${json}, \"description\": \"$description\""
json="${json}, \"startDateTime\": \"$startDateTime\""
json="${json}, \"endDateTime\": \"$endDateTime\""
json="${json}, \"eventType\": \"$eventType\""
json="${json}, \"maxAttendees\": $maxAttendees"
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

curl -X POST -H "$authHeader" -H "$contentTypeHeader" -d "$json" http://localhost:8080/api/v1/events
echo
#!/bin/sh

if [ -z "$1" ]; then
	echo "Need to supply a JSON Web Token"
	exit 1;
fi

if [ -z "$2" ]; then
	echo "Need to supply a name"
	exit 1;
fi

if [ -z "$3" ]; then
	echo "Need to supply a description"
	exit 1;
fi

if [ -z "$4" ]; then
	echo "Need to supply a start date and time"
	exit 1;
fi

if [ -z "$5" ]; then
	echo "Need to supply an end date and time"
	exit 1;
fi

if [ -z "$6" ]; then
	echo "Need to supply an event type"
	exit 1;
fi

if [ -z "$7" ]; then
	echo "Need to supply a max number of attendees"
	exit 1;
fi

if [ -z "$8" ]; then
	echo "Need to supply a 1st address line"
	exit 1;
fi

if [ -z "$9" ]; then
	echo "Need to supply a 2nd address line"
	exit 1;
fi

if [ -z "${10}" ]; then
	echo "Need to supply a city"
	exit 1;
fi

if [ -z "${11}" ]; then
	echo "Need to supply a state"
	exit 1;
fi

if [ -z "${12}" ]; then
	echo "Need to supply a postal code"
	exit 1;
fi

if [ -z "${13}" ]; then
	echo "Need to supply a country"
	exit 1;
fi

if [ -z "${14}" ]; then
	echo "Need to supply a latitude"
	exit 1;
fi

if [ -z "${15}" ]; then
	echo "Need to supply a longitude"
	exit 1;
fi

authHeader="Authorization: Bearer $1"
contentTypeHeader="Content-Type: application/json"
json="{\"name\": \"$2\","
json="${json} \"description\": \"$3\","
json="${json} \"startDateTime\": \"$4\","
json="${json} \"endDateTime\": \"$5\","
json="${json} \"eventType\": \"$6\","
json="${json} \"maxAttendees\": $7,"
json="${json} \"addressLine1\": \"$8\","
json="${json} \"addressLine2\": \"$9\","
json="${json} \"city\": \"${10}\","
json="${json} \"state\": \"${11}\","
json="${json} \"postalCode\": \"${12}\","
json="${json} \"country\": \"${13}\","
json="${json} \"latitude\": ${14},"
json="${json} \"longitude\": ${15} }"

curl -X POST -H "$authHeader" -H "$contentTypeHeader" -d "$json" http://localhost:8080/api/v1/events

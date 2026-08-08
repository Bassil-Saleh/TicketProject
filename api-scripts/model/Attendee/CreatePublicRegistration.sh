#!/bin/sh

# Defaults
eventPublicId=""
firstName=""
middleName=""
lastName=""
email=""

# Parse named parameters
while [ $# -gt 0 ]; do
	case "$1" in
		--event-public-id) eventPublicId="$2"; shift 2 ;;
		--first-name)      firstName="$2";     shift 2 ;;
		--middle-name)     middleName="$2";    shift 2 ;;
		--last-name)       lastName="$2";      shift 2 ;;
		--email)           email="$2";         shift 2 ;;
		*)                 shift ;;
	esac
done

# Validate mandatory parameters
if [ -z "$eventPublicId" ] || [ -z "$firstName" ] || [ -z "$lastName" ] || [ -z "$email" ]; then
	echo "Usage: $0 --event-public-id <value> --first-name <value> --last-name <value> --email <value> [--middle-name <value>]"
	exit 1
fi

# Build JSON (only include middleName if provided)
json="{\"firstName\": \"$firstName\""
if [ -n "$middleName" ]; then
	json="${json}, \"middleName\": \"$middleName\""
fi
json="${json}, \"lastName\": \"$lastName\""
json="${json}, \"email\": \"$email\""
json="${json}, \"eventPublicId\": \"$eventPublicId\""
json="${json}}"

curl --json "$json" http://localhost:8080/api/v1/attendees/registration
echo
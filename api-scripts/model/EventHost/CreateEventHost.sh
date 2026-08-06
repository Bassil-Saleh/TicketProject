#!/bin/sh

# Defaults
firstName=""
middleName=""
lastName=""
email=""
password=""
dateOfBirth=""

# Parse named parameters
while [ $# -gt 0 ]; do
	case "$1" in
		--first-name)    firstName="$2";    shift 2 ;;
		--middle-name)   middleName="$2";   shift 2 ;;
		--last-name)     lastName="$2";     shift 2 ;;
		--email)         email="$2";        shift 2 ;;
		--password)      password="$2";     shift 2 ;;
		--date-of-birth) dateOfBirth="$2";  shift 2 ;;
		*)               shift ;;
	esac
done

# Validate mandatory parameters
if [ -z "$firstName" ] || [ -z "$lastName" ] || [ -z "$email" ] || [ -z "$password" ] || [ -z "$dateOfBirth" ]; then
	echo "Usage: $0 --first-name <value> --last-name <value> --email <value> --password <value> --date-of-birth <value> [--middle-name <value>]"
	exit 1
fi

# Build JSON (only include middleName if provided)
json="{\"firstName\": \"$firstName\""
if [ -n "$middleName" ]; then
	json="${json}, \"middleName\": \"$middleName\""
fi
json="${json}, \"lastName\": \"$lastName\""
json="${json}, \"email\": \"$email\""
json="${json}, \"password\": \"$password\""
json="${json}, \"dateOfBirth\": \"$dateOfBirth\"}"

curl --json "$json" http://localhost:8080/api/v1/event-hosts
echo
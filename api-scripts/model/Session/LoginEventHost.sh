#!/bin/sh

# Defaults
email=""
password=""

# Parse named parameters
while [ $# -gt 0 ]; do
	case "$1" in
		--email)    email="$2";    shift 2 ;;
		--password) password="$2"; shift 2 ;;
		*)          shift ;;
	esac
done

# Validate mandatory parameters
if [ -z "$email" ] || [ -z "$password" ]; then
	echo "Usage: $0 --email <value> --password <value>"
	exit 1
fi

json="{\"email\": \"$email\", \"password\": \"$password\"}"

curl --json "$json" http://localhost:8080/api/v1/sessions/login
echo
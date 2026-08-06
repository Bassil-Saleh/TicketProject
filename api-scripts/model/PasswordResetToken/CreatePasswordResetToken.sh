#!/bin/sh

# Defaults
email=""

# Parse named parameters
while [ $# -gt 0 ]; do
	case "$1" in
		--email) email="$2"; shift 2 ;;
		*)       shift ;;
	esac
done

# Validate mandatory parameters
if [ -z "$email" ]; then
	echo "Usage: $0 --email <value>"
	exit 1
fi

json="{\"email\": \"$email\"}"

curl --json "$json" http://localhost:8080/api/v1/password-reset-tokens
echo
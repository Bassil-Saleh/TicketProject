#!/bin/sh

# Defaults
publicToken=""
invitationResponse=""

# Parse named parameters
while [ $# -gt 0 ]; do
	case "$1" in
		--public-token)        publicToken="$2";        shift 2 ;;
        --invitation-response) invitationResponse="$2"; shift 2 ;;
		*)                     shift ;;
	esac
done

# Validate mandatory parameters
if [ -z "$publicToken" ] || [ -z "$invitationResponse" ]; then
    echo "Usage: $0 --public-token <value> --invitation-response <value>"
    exit 1
fi

url="http://localhost:8080/api/v1/tickets/invitation"
url="${url}?publicToken=${publicToken}"
url="${url}&invitationResponse=${invitationResponse}"

curl -X PATCH "$url"
echo
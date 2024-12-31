#!/bin/bash

# Jellyfin base URL and API Key
BASE_URL="https://my.jellyfin"
API_KEY="xxxxxxxx"

# step 1: Get all Persons Id
echo "Fetching Ids from Persons API..."
response=$(curl -s "${BASE_URL}/Persons?api_key=${API_KEY}")
ids=$(echo "$response" | jq -r '.Items[].Id')

# check jq
if ! command -v jq &>/dev/null; then
    echo "jq not installed, please install jq first (sudo apt install jq)" >&2
    exit 1
fi

# traverse the Id set
for id in $ids; do
    echo "Processing Id: $id"

    # Step 2: Check if the image exists, send a GET request
    echo "Checking if Primary Image exists for Id: $id..."
    image_response=$(curl -s -o /dev/null -w "%{http_code}" "${BASE_URL}/Items/${id}/Images/Primary?api_key=${API_KEY}")

    if [[ "$image_response" == "200" ]]; then
        echo "Primary Image exists for Id: $id. Skipping..."
        continue
    fi

    # Step 3: Delete Image (DELETE request)
    echo "Deleting Primary Image for Id: $id..."
    delete_response=$(curl -s -X DELETE -o /dev/null -w "%{http_code}" "${BASE_URL}/Items/${id}/Images/Primary?api_key=${API_KEY}")

    if [[ "$delete_response" != "204" ]]; then
        echo "Failed to delete Primary Image for Id: $id. Skipping..."
        continue
    fi
    echo "Successfully deleted Primary Image for Id: $id."

    # Step 4: Check the new resource (Users API) until status code 200 is returned.
    echo "Waiting for resource to be available for Id: $id..."
    while true; do
        user_response=$(curl -s -o /dev/null -w "%{http_code}" "${BASE_URL}/Users/6a483b139f514ce7abb2c4d6654a2ede/Items/${id}?api_key=${API_KEY}")
        if [[ "$user_response" == "200" ]]; then
            echo "Resource is ready for Id: $id."
            break
        fi
        echo "Resource not available yet for Id: $id. Retrying in 3 seconds..."
        sleep 3
    done
done

echo "All processing completed."
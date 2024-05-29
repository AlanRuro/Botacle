#!/bin/bash

# Set the user ID and desired description
USER_ID=ocid1.user.oc1..aaaaaaaaqnyxzqetqlgdh5qt2ex375xqyt232kn74i6quzaah5ft7yc2ogwq
TOKEN_DESCRIPTION="vm docker login"

# List auth tokens and extract the id of the token with the specific description
TOKEN_ID=$(oci iam auth-token list --user-id "$USER_ID" --query "data[?description=='$TOKEN_DESCRIPTION'].id | [0]" --raw-output)

# Check if TOKEN_ID is not empty
if [ -n "$TOKEN_ID" ]; then
  echo "Found auth token with description '$TOKEN_DESCRIPTION'."

  # Delete the auth token
  oci iam auth-token delete --user-id "$USER_ID" --auth-token-id "$TOKEN_ID" --force
  echo "Auth token with description '$TOKEN_DESCRIPTION' has been deleted."
else
  echo "No auth token found with description '$TOKEN_DESCRIPTION'."
fi

# Create a new auth token and store it in the TOKEN variable
TOKEN=$(oci iam auth-token create --user-id "$USER_ID" --description '$TOKEN_DESCRIPTION' --query 'data.token' --raw-output)

# Retry logic for Docker login
RETRIES=0
while test $RETRIES -le 30; do
  if echo "$TOKEN" | docker login -u "axcioc1wifb3/a01641788@tec.mx" --password-stdin "mx-queretaro-1.ocir.io"; then
    echo "Docker login completed"
    export DOCKER_REGISTRY=mx-queretaro-1.ocir.io/axcioc1wifb3/reacttodo/gw5ok
    export OCI_CLI_PROFILE=mx-queretaro-1
    break
  else
    echo "Docker login failed. Retrying"
    RETRIES=$((RETRIES+1))
    sleep 5
  fi
done

if [ $RETRIES -gt 30 ]; then
  echo "Docker login failed after maximum retries."
  exit 1
fi

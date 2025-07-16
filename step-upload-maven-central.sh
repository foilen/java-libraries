#!/bin/bash

set -e

RUN_PATH="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd $RUN_PATH

# Verify environment variables
if [ -z "$OSSRH_USER" ] || [ -z "$OSSRH_PASS" ]; then
    echo "Error: OSSRH_USER and OSSRH_PASS environment variables must be set."
    exit 1
fi

echo ----[ Upload to Maven Central ]----
./gradlew publish

echo ----[ Release it ]----
# Generate base64 token from credentials
AUTH_TOKEN=$(echo -n "${OSSRH_USER}:${OSSRH_PASS}" | base64)
curl -v -X POST \
  -H "Authorization: Bearer ${AUTH_TOKEN}" \
  "https://ossrh-staging-api.central.sonatype.com/manual/upload/defaultRepository/com.foilen?publishing_type=automatic"

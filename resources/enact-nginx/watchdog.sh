#!/bin/bash

if [ $# -ne 1 ]
then
    echo "Error: Wrong number of arguments."
    echo "  Usage:  ./watchdog.sh ENDPOINT"
    exit 1
fi

ENDPOINT="${1}"

WORKING_DIRECTORY=$(dirname "${0}")
ENDPOINT_LIST="${WORKING_DIRECTORY}/endpoints.dat"
ENDPOINT_MANAGER="${WORKING_DIRECTORY}/endpoints.sh"
HEALTH_CHECK="${WORKING_DIRECTORY}/healthcheck.sh"

# Extract the status (up or down) of the the endpoint
previous_status=$(cat "${ENDPOINT_LIST}" \
		      | grep "${ENDPOINT}" \
		      | cut -d ' ' -f2)
if [ -z "${previous_status}" ]
then
    previous_status='down'
fi

# Invoke the health check

bash "${HEALTH_CHECK}" "${ENDPOINT}"


# Compare the previous status with health check result and send the
# appropriate message to the endpoint manager.
if [ $? -ne 0 ]
then 
    if [ "${previous_status}" = "up" ]
    then
	echo "$(date) Endpoint '${ENDPOINT}' has failed!"
	bash "${ENDPOINT_MANAGER}" failure-of "${ENDPOINT}"
    fi
else
    if [  "${previous_status}" = "down" ]
    then
	echo "$(date) Endpoint '${ENDPOINT}' is back!"
	bash "${ENDPOINT_MANAGER}" repair-of "${ENDPOINT}"
    fi
fi
	

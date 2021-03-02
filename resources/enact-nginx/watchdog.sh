#!/bin/bash

if [ $# -ne 1 ]
then
    echo "Error: Wrong number of arguments."
    echo "  Usage:  ./watchdog.sh ENDPOINT"
    exit 1;
fi

ENDPOINT="${1}"

WORKING_DIRECTORY=$(realpath $(dirname "${0}"));
ENDPOINT_LIST="${WORKING_DIRECTORY}/endpoints.dat"
ENDPOINT_MANAGER="${WORKING_DIRECTORY}/endpoints.sh"
HEALTH_CHECK="${WORKING_DIRECTORY}/healthcheck.sh"


log () {
    local category="${1}"
    local message="${2}";
    local timestamp=$(date);
    echo "${timestamp} ${category}: ${message}";
}

# Check if the health check script is available
if [ ! -e "${HEALTH_CHECK}" ]
then
    log "ERROR" "Could  not find '${HEALTH_CHECK}'!"
    exit 2;
fi

# Check if the endpoint manager is available
if [ ! -e "${ENDPOINT_MANAGER}" ]
then
    log "ERROR" "Could not find the manager script '${ENDPOINT_MANAGER}'";
    exit 3;
fi

# Check if the endpoint list is available
if [ ! -e "${ENDPOINT_LIST}" ]
then
    log "ERROR" "Could not find the endpoint list '${ENDPOINT_LIST}'";
    exit 4;
fi

   
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
status=$?;
log "INFO" "Heath check for '${ENDPOINT}' returned '${status}'";

# Compare the previous status with health check result and send the
# appropriate message to the endpoint manager.
if [ $status -ne 0 ]
then 
    if [ "${previous_status}" = "up" ]
    then
	log "INFO" "Endpoint '${ENDPOINT}' has failed!"
	bash "${ENDPOINT_MANAGER}" failure-of "${ENDPOINT}"
    fi
else
    if [ "${previous_status}" = "down" ]
    then
	log  "INFO" "Endpoint '${ENDPOINT}' is back!"
	bash "${ENDPOINT_MANAGER}" repair-of "${ENDPOINT}"
    fi
fi
	

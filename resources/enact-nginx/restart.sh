#!/bin/bash

# This file is part of the ENACT-Nginx distribution.

# It restarts the restart a replica, given the docker host, the image
# to use and the name of the container. It first try to stop and
# remove the container, and then create a new container.

# USAGE: bash restart.sh [CONFIG_FILE] [CONTAINER_NAME]


RESPONSE_FILE='response.txt'

# Error codes
UNKNOWN_ERROR=1;
SERVER_ERROR=2;
NO_SUCH_CONTAINER=3;
CONTAINER_EXISTS=4;



load_docker_configuration () {
    local configuration_file="${1}";
    if [ ! -e "${configuration_file}" ];
    then
	echo "Error: Cannot find configuration file '${configuration_file}'";
	exit   1;
    fi
    source "${configuration_file}";
    echo "Configuration loaded from '${configuration_file}':" > /dev/tty
    echo " - DOCKER_HOST: '${DOCKER_HOST}'" > /dev/tty;
    echo " - IMAGE: '${IMAGE_NAME}'" > /dev/tty;
}


unexpected_error () {
    local description=$1;
    local status_code=$2;
    local docker_host=$3;
    local error_message=$(cat "${RESPONSE_FILE}" | jq .message);
    echo "Error: ${description}." > /dev/tty;
    echo "       Received HTTP code '${status_code}' from host '${docker_host}'." > /dev/tty;
    if [ -s "${RESPONSE_FILE}"  ]
    then
	echo "       ${error_message}" > /dev/tty;
    else
	echo "       No response received.";
    fi
}


stop_container () {
    local docker_host=$1;
    local containerID=$2;
    local status_code=$(curl --write-out '%{http_code}' --silent --output "${RESPONSE_FILE}" \
			     --header "Content-Type: application/json" \
			     --request POST "http://${docker_host}/containers/${containerID}/stop?t=1");
    case "${status_code}" in
	200|204|304)
	    echo "Container '${containerID}' stopped." > /dev/tty;
	    return 0;
	    ;;
	404)
	    echo "Error: Could not stop container '${container_name}'." > /dev/tty;
	    echo "       There is no such container (code 404)'." > /dev/tty;	    
	    return ${NO_SUCH_CONTAINER};
	    ;;
	500)
	    echo "Error: Could not stop container '${container_name}'." > /dev/tty;
	    echo "       Internal server error (code 500)'." > /dev/tty;	    
	    return ${SERVER_ERROR};
	    ;;
	*)
	    unexpected_error "Could not stop container '${container_name}'" \
			     "${status_code}" "${docker_host}";
	    return ${UNKNOWN_ERROR}
	    ;;
    esac
}


remove_container () {
    local docker_host=$1;
    local containerID=$2;
    status_code=$(curl --write-out '%{http_code}' --silent --output "${RESPONSE_FILE}" \
		       --header "Content-Type: application/json" \
		       --request DELETE "http://${docker_host}/containers/${containerID}?v=true&force=true");
    
    case "${status_code}" in
	200|204)
	    echo "Container '${containerID}' removed." > /dev/tty;
	    return 0;
	    ;;
	404)
	    echo "Error: Could not remove container '${container_name}'." > /dev/tty;
	    echo "       There is no such container (code 404)'." > /dev/tty;	    
	    return ${NO_SUCH_CONTAINER};
	    ;;
	500)
	    echo "Error: Could not remove container '${container_name}'." > /dev/tty;
	    echo "       Internal server error (code 500)'." > /dev/tty;	    
	    return ${SERVER_ERROR};
	    ;;
	*)
	    unexpected_error  "Could not remove container '${container_name}'" \
			      "${status_code}" "${docker_host}";
	    return ${UNKNOWN_ERROR};
	    ;;
    esac
}


create_container () {
    local docker_host=$1;
    local image_name=$2;
    local container_name=$3;
    local status_code=$(curl --write-out '%{http_code}' --silent --output "${RESPONSE_FILE}" \
			     --header "Content-Type: application/json" \
			     --data "{\"Image\": \"${image_name}\"}" \
			     --request POST "http://${docker_host}/containers/create?name=${container_name}");
    case "${status_code}" in
	200|201)
	    local containerID=$(cat "${RESPONSE_FILE}" | jq .Id);
	    echo "New container '${container_name}' from image '${image_name}' created!" > /dev/tty;
	    echo "ID: ${containerID}" > /dev/tty;
	    return 0;
	    ;;
	409)
	    echo "Error: Container '${container_name}' aleady exist!"  > /dev/tty;
	    return ${CONTAINER_EXISTS};
	    ;;
	*)
	    unexpected_error  "Could not create container '${container_name}'" \
			      "${status_code}" "${docker_host}";
	    return ${UNKNOWN_ERROR};
	    ;;
    esac    
}


start_container () {
    local docker_host=$1;
    local container_name=$2;
    local status_code=$(curl --write-out '%{http_code}' --silent --output "${RESPONSE_FILE}" \
			     --header "Content-Type: application/json" \
			     --request POST "http://${docker_host}/containers/${container_name}/start");
    case "${status_code}" in
	200|204|304)
	    echo "Container '${container_name}' started!" > /dev/tty;
	    return 0;
	    ;;
	404)
	    echo "Error: Cannot start container '${container_name}'"  > /dev/tty;
	    echo "       No such container (code 404)"  > /dev/tty;
	    return ${NO_SUCH_CONTAINER};
	    ;;
	*)
	    unexpected_error  "Could not start container '${container_name}'" \
			      "${status_code}" "${docker_host}";
	    return ${UNKNOWN_ERROR};
	    ;;
    esac
    
}


# Main script

if [ $# != 2 ]
then
    echo "Usage: bash restart.sh [CONFIG_FILE] [CONTAINER_NAME]";
    echo "Example: bash restart.sh docker.sh a_component-3";
    exit 1;
fi
   
CONFIGURATION_FILE=$1;
CONTAINER_NAME=$2;

load_docker_configuration "${1}";

stop_container "${DOCKER_HOST}" "${CONTAINER_NAME}";
error=$?;
if [ $error -eq 0 ]
then
    remove_container "${DOCKER_HOST}" "${CONTAINER_NAME}";
    if [ $? -ne 0 ]
    then
	exit 1;
    fi
elif [ $error -ne ${NO_SUCH_CONTAINER} ]
then
    exit 1;
fi


container_id=$(create_container "${DOCKER_HOST}" "${IMAGE_NAME}" "${CONTAINER_NAME}");
if [ $? -ne 0 ]
then
    exit 1;
fi
start_container "${DOCKER_HOST}" "${CONTAINER_NAME}";

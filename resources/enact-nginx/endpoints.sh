#!/bin/bash

#DEBUG='yes'

# The NGinx configuration file
NGINX_CONFIG_FILE='/etc/nginx/nginx.conf'
DEBUG_CONFIG_FILE='debug.conf'

# The configuration template for the Nginx provided by ENACT
ENACT_TEMPLATE_CONFIG='/etc/nginx/enact_template.conf'


if [ -n "${DEBUG}" ]
then
    if [ ! -f "${DEBUG_CONFIG_FILE}" ]
    then
	cp 'nginx.conf' "${DEBUG_CONFIG_FILE}"
    fi
    NGINX_CONFIG_FILE="${DEBUG_CONFIG_FILE}"
    ENACT_TEMPLATE_CONFIG='enact_template.conf'
fi  

# The file that contains the list of endpoints/replicas 
WORKING_DIRECTORY=$(realpath $(dirname "${0}"))
ENDPOINT_FILE="${WORKING_DIRECTORY}/endpoints.dat"

# Port on which the proxy listens.
ENACT_PROXY_PORT=5000

DOCKER_CONFIG="docker.sh";

# Store the configuration of the remote Docker API (its endpoint),
# together with the name of the Docker image used to instantiate
# replicas
configure_docker_api () {
    local DOCKER_HOST="${1}";
    local IMAGE_NAME="${2}";
    local COMMAND="${3}";
    (echo "DOCKER_HOST='${DOCKER_HOST}'"
     echo "IMAGE_NAME='${IMAGE_NAME}'"
     echo "COMMAND='${COMMAND}'") > "${DOCKER_CONFIG}";
    echo "Configuration stored in '${DOCKER_CONFIG}'";
}


# Check that the given endpoint does appear in the list of known
# endpoints.
is_known () {
    local target_endpoint="${1}"
    if grep -q "${target_endpoint}" "${ENDPOINT_FILE}"
    then
	return 0
    fi
    return 1
}


# Search the NGINX_CONFIG_FILE for one of the known endpoints (i.e., those
# listed in the ENDPOINT_FILE.
find_active () {
    while read entry || [ -n "${entry}" ]
    do
	if [[ "${entry}" =~ [$\#] ]] || [ -z "${entry}" ]
	then
	    continue
	else
	    local endpoint=$(echo "${entry}" | cut -d ' ' -f1)
	    local status=$(echo "${entry}" | cut -d ' ' -f2)
	    if grep -q "${endpoint}" ${NGINX_CONFIG_FILE}
	    then
		echo "${endpoint}"
		return 0
	    fi
	fi
    done <${ENDPOINT_FILE}
    return 1
}


# Find the endpoint that precedes the given one in the list of known
# endpoints
find_backup_of () {
    local current_endpoint="${1}"
    local backup=""
    while read entry || [ -n "${entry}" ]
    do
	if [[ "${entry}" =~ [$\#] ]]
	then
	    continue
	else
	    local endpoint=$(echo "${entry}" | cut -d ' ' -f1)
	    local status=$(echo "${entry}" | cut -d ' ' -f2)
	    
	    if [[ "${endpoint}" = "${current_endpoint}" ]]
	    then
		if [ ! -z ${backup}  ]
		then
		    echo "${backup}"
		    return 0
		else
		    echo "Error: '${current_endpoint}' is the first available endpoint." > /dev/tty
		    return 2
		fi
	    fi

	    if [ "${status}" = "up" ]
	    then
		backup="${endpoint}"
	    fi
	fi
    done < "${ENDPOINT_FILE}"

    echo "Error: Could not found endpoint '${current_endpoint}'" > /dev/tty
    return 1
}


# Search the the list of endpoint for the last one that is "up", that
# is the possible ugrade.
find_upgrade () {
    local upgrade=""
    while read entry || [ -n "${entry}" ]
    do
	if [[ "${entry}" =~ [$\#] ]]
	then
	    continue
	else
	    local endpoint=$(echo "${entry}" | cut -d ' ' -f1)
	    local status=$(echo "${entry}" | cut -d ' ' -f2)
	    if [ "${status}" = "up" ]
	    then
		upgrade="${endpoint}"
	    fi
	fi
    done < "${ENDPOINT_FILE}"

    if [ -z "${upgrade}" ]
    then
	echo "Could not find any upgrade." >> /dev/tty
	return 1
    fi
    echo "${upgrade}"
}


# Restart the NGinx server, if NOT in debug mode. Display the server
# configuration otherwise.
restart_nginx () {
    if [ -z "${DEBUG}" ]
    then
	nginx -s reload
    else
	show_proxy_config
    fi
}


# Activate the endpoint given as first parameter
activate () {
    local target_endpoint="${1}"

    register "${target_endpoint}"
    
    local active_endpoint
    active_endpoint=$(find_active)
    if [ $? -ne 0 ]
    then
	echo "Info: No endpoint is active yet!" >/dev/tty
    fi

    if [ "${active_endpoint}" != "${target_endpoint}" ]
    then
	if [ -z "${active_endpoint}" ]
	then
	    echo "Activating '${target_endpoint}'"
	else
	    echo "Switching from '${active_endpoint}' to '${target_endpoint}' ... "
	fi
	export ENACT_PROXY_PORT="${ENACT_PROXY_PORT}"
	export ENACT_SELECTED_ENDPOINT="${target_endpoint}"
	envsubst '${ENACT_PROXY_PORT}${ENACT_SELECTED_ENDPOINT}' < "${ENACT_TEMPLATE_CONFIG}" > "${NGINX_CONFIG_FILE}"

	restart_nginx
	
	echo "Done"
    else
	echo "Endpoint '${target_endpoint}' is already active!"
    fi
}


# Remove the given endpoint from the list of known endpoints
discard ()  {
    local endpoint="${1}"
    is_known "${endpoint}"
    if [ $? -ne 0 ]
    then
	echo "Error: Could not find endpoint '${endpoint}'."
	return 1
    fi
    sed -i "\|${endpoint}|d" "${ENDPOINT_FILE}"
    echo "Endpoint '${endpoint}' removed."

    tmp_file='cronjob.txt'
    crontab -l 2> /dev/null | sed "\|${endpoint} .*|d" > "${tmp_file}"
    crontab < "${tmp_file}"
    rm -f "${tmp_file}"
    echo "Watchdog disabled for endpoint '${endpoint}'"

    return 0
}


# Initialize the proxy configuration with the given PORT and active
# endpoint
initialize () {
    ENACT_PROXY_PORT="${1}";
    local active_endpoint="${2}";

    activate "${active_endpoint}";
    service cron restart;
}


# Update the endpoint list: Adjust the status of the given endpoint.
mark () {
    local endpoint="${1}"
    local status="${2}"
    sed -i "s/${endpoint} .*/${endpoint} ${status}/" "${ENDPOINT_FILE}"
}


# React to the failure of the given endpoint: If the given endpoint is
# the active one, it activates the previous one.
on_failure_of () {
    local failed_endpoint="${1}"
    is_known "${failed_endpoint}"
    if [ $? -ne 0 ]
    then
	echo "Unknown endpoint '${failed_endpoint}'."
	return 1
    fi

    mark "${failed_endpoint}" "down"

    # Try restarting the failed endpoint!
    local container_name=$(echo "${failed_endpoint}" | sed -e 's/:.*//');
    bash "${WORKING_DIRECTORY}/restart.sh" \
	 "${WORKING_DIRECTORY}/${DOCKER_CONFIG}" \
	 "${container_name}";

    # Meanwhile, we search for a replacement
    local active_endpoint
    active_endpoint=$(find_active)
    if [ $? -ne 0 ]
    then
	echo "Could not identify the active endpoint."
	return 2
    fi

    if [ "${failed_endpoint}" = "${active_endpoint}" ]
    then
	local replacement
	replacement=$(find_upgrade)
	if [ $? -ne 0 ]
	then
	    echo "No backup endpoint for '${active_endpoint}'."
	    return 3
	else
	    activate "${replacement}"
	fi
    fi
}


# React to the "repair" of the given endpoint, that is when it is back
# online.
on_repair_of () {
    local repaired_endpoint="${1}"
    is_known "${repaired_endpoint}"
    if [ $? -ne 0 ]
    then
	exit 1
    fi

    mark "${repaired_endpoint}" "up"

    local replacement_endpoint
    upgrade_endpoint=$(find_upgrade)
    if [ $? -ne 0 ]
    then
	echo "Could not upgrade to other endpoint."
	exit 2
    fi

    activate ${upgrade_endpoint}
}


WATCHDOG="${WORKING_DIRECTORY}/watchdog.sh"

# Register a new endpoint in the list of known endpoints, and add a
# cron job that triggers the watchdog on a regular basis.
register () {
    local new_endpoint="${1}"
    is_known "${new_endpoint}"
    if [ $? -eq 0 ]
    then
	echo "Endpoint already known."
	return 1
    fi

    # Append the new endpoint to the list
    sed -i '$a\' "${ENDPOINT_FILE}"
    echo "${new_endpoint} down" >> "${ENDPOINT_FILE}"
    echo "Endpoint '${new_endpoint} added to '${ENDPOINT_FILE}'."

    # Add a CRON job to check the status of the endpoint at a fixed interval
    local replica_name=$(echo "${new_endpoint}" | sed -r 's/:.*//');
    local log_file="${WORKING_DIRECTORY}/watchdog_${replica_name}.log";
    local tmp_file='cronjob.txt'
    crontab -l  2>/dev/null | sed "\|${new_endpoint} .*|d" > "${tmp_file}"
    echo "* * * * * ${WATCHDOG} ${new_endpoint} 2>&1 >> ${log_file}" >> "${tmp_file}"
    crontab < "${tmp_file}"
    rm -f "${tmp_file}"
    echo "Watchdog setup for endpoint '${new_endpoint}'."
}


# List the endpoints stored in the 'known_endpoints.dat' file
show() {
    echo "Endpoints from '${ENDPOINT_FILE}'"
    if [[ ! -f "${ENDPOINT_FILE}" ]]
    then
	echo "Cannot access '${ENDPOINT_FILE}'."
	return 1
    fi
    while read endpoint || [ -n "${endpoint}" ]
    do
	if [[ "${endpoint}" =~ [$\#] ]]
	then
	    continue
	else
	    echo "${endpoint}"
	fi
    done < "${ENDPOINT_FILE}"
}


# Show the endpoint currently used by the proxy
show_active () {
    local active_endpoint;
    active_endpoint=$(find_active)
    if [ $? -ne 0 ]
    then
	exit 1
    fi
    echo "${active_endpoint}"
}


# Show the previous endpoint, that is the one that precedes the active
# one in the list of known endpoints.
show_backup () {
    local active_endpoint
    active_endpoint=$(find_active)
    if [ $? -ne 0 ]
    then
	exit 1
    fi
    backup_endpoint=$(find_backup_of "${active_endpoint}")
    if [ $? -ne 0 ]
    then
	exit 1
    fi
    echo "${backup_endpoint}"
}


# Show the configuration of the proxy
show_proxy_config ()  {
    cat "${NGINX_CONFIG_FILE}"
}


# Show the possible "upgrade" endpoint
show_upgrade () {
    local upgrade_endpoint
    upgrade_endpoint=$(find_upgrade)
    if [ $? -ne 0 ]
    then
	exit 1
    fi
    echo "${upgrade_endpoint}"
}


# Show the usage of this script
usage () {
    echo "Usage: endpoints.sh [CMD] [OPTIONS]"
    echo ""
    echo "where CMD is one the following: "
    echo "  activate [ENDPOINT]"
    echo "      activate the given endpoint;"
    echo ""
    echo "  configure-docker [REMOTE_API] [IMAGE_NAME] [START_COMMAND]"
    echo "      save the configuration of the remote Docker engine, the name of the"
    echo "      Docker image used to instantiate them, as well as the command to "
    echo "      start the container. These are needed to restart replicas on failure."
    echo ""
    echo "  discard [ENDPOINT]"
    echo "      remove the given endpoint;"
    echo ""
    echo "  failure-of [ENDPOINT]"
    echo "      react to a failure of the given endpoint;"
    echo ""
    echo "  initialize [PROXY_PORT] [ACTIVE_ENDPOINT]"
    echo "      initialize the proxy listening on a given PROXY_PORT and forwarding"
    echo "      to the given ACTIVE_ENDPOINT;"
    echo ""
    echo "  register [ENDPOINT]"
    echo "      register a new endpoint in the list;"
    echo ""
    echo "  repair-of [ENDPOINT]"
    echo "      react to the repair of the given endpoint;"
    echo ""
    echo "  show                   how the list of known endpoints;"
    echo ""
    echo "  show-active            show the endpoint currently used;"
    echo ""
    echo "  show-backup            show the backup endpoint in the list;"
    echo ""
    echo "  show-proxy             show the configuration of the proxy;"
    echo ""
    echo "  show-upgrade           show the possible upgrade endpoint."
}


while [[ $# -gt 0 ]]
do
    case "${1}" in
	activate)
	    activate "${2}"
	    shift 2
	    ;;
	configure-docker)
	    configure_docker_api "${2}" "${3}" "${4}";
	    shift 4;
	    ;;
	discard)
	    discard "${2}"
	    shift 2
	    ;;
	failure-of)
	    on_failure_of "${2}"
	    shift 2
	    ;;
	initialize)
	    initialize  "${2}" "${3}"
	    shift 3
	    ;;
	register)
	    register "${2}"
	    shift 2
	    ;;
	repair-of)
	    on_repair_of "${2}"
	    shift 2
	    ;;
	show)
	    show
	    shift
	    ;;
	show-active)
	    show_active
	    shift
	    ;;
	show-backup)
	    show_backup
	    shift
	    ;;
	show-proxy)
	    show_proxy_config
	    shift
	    ;;
	show-upgrade)
	    show_upgrade
	    shift
	    ;;
	*)
	    echo "Unknown command '${1}'."
	    usage
	    shift
	    ;;
    esac
done

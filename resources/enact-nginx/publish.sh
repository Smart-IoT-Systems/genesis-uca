#!/bin/bash
 
# Rebuild and publish the "enact-nginx" Docker image for the local
# files.

SCRIPT="${0}"

VERSION=1.0.8


bump_version() {
    local release="${1}"
    local version=$(sed -n -r 's/^VERSION=([0-9]+\.[0-9]+\.[0-9]+)\s*$/\1/p' "${SCRIPT}")
    local major=$(echo "${version}" | sed -n -r 's/([0-9]+)\.[0-9]+\.[0-9]+/\1/p')
    local minor=$(echo "${version}" | sed -n -r 's/[0-9]+\.([0-9]+)\.[0-9]+/\1/p')
    local patch=$(echo "${version}" | sed -n -r 's/[0-9]+\.[0-9]+\.([0-9]+)+/\1/p')
    case "${release}" in
	major)
	    major=$((major + 1))
	    ;;
	minor)
	    minor=$((minor + 1))
	    ;;
	*)
	    patch=$((patch + 1))
	    ;;
    esac
    VERSION="${major}.${minor}.${patch}"
    echo "Bumping to v${VERSION} (was $version)."
}


override_version() {
    sed -i -r "s/VERSION=[0-9]+\.[0-9]+\.[0-9]+\s*$/VERSION=${VERSION}/g" "${SCRIPT}"
    echo "Version updated to ${VERSION} in '${SCRIPT}'."
}


REPOSITORY='fchauvel'
IMAGE_NAME='enact-nginx'
LATEST_DOCKER_IMAGE="${REPOSITORY}/${IMAGE_NAME}:latest"
VERSION_DOCKER_IMAGE="${REPOSITORY}/${IMAGE_NAME}:${VERSION}"

set -e  # Stop at the first error

bump_version "${1}"


# Clear the endpoints.dat file
sed -i '/^#/!d' endpoints.dat

# Ensure the DEBUG mode is disabled in the endpoints.sh script
sed -i "s/^DEBUG='yes'/#DEBUG='yes'/" endpoints.sh

# Rebuild the docker image
docker build -t "${LATEST_DOCKER_IMAGE}" -t "${VERSION_DOCKER_IMAGE}" .

# Push the image
docker login
docker push "${LATEST_DOCKER_IMAGE}"
docker push "${VERSION_DOCKER_IMAGE}"

override_version

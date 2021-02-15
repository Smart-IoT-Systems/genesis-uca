# Build a specific nginx-image that includes shell scripts that manage
# components' replicas for the ENACT builtin availability mechanisms

FROM nginx:1.19.6

RUN apt-get update \
	&& apt-get install --yes --no-install-recommends cron jq \
	&& apt-get autoremove --yes

WORKDIR /enact

COPY ["./nginx.conf", "./enact_template.conf", "/etc/nginx/"]
COPY ["./endpoints.sh", "./endpoints.dat", "./watchdog.sh", "restart.sh", "/enact/"] 

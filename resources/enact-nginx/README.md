# ENACT NGinx 

This is a Docker image of the proxy that is used by the builtin
availability mechanisms provided by GeneSIS. That is, if you specify
"Builtin" in the availability, GeneSIS will deploy a NGinx server
acting as a "load balancer" or proxy in front of the replicas.

 
Here are the files together with a short description:

 * `endpoints.sh` this the file that control the addition
   (resp. removal) of endpoints/replicas. There is no need to edit
   `endpoints.dat` by hand.
   
 * `endpoints.dat` is a text file that contains the list of
   replicas/endpoints together with their status: down if the last
   health check failed, and up if it succeed.
   
 * `watchdog.sh` is a Bash script that is triggered by CRON at fixed
   intervals. In turn, it triggers the health check (yet another
   script, but provided by the user and uploaded in the container by
   GeneSIS), and triggers `endpoints.sh` depending on the health
   status.
   
 * `publish.sh` is a Bash script that automatically build and publish
   the enact-nginx image.
   
 * `nginx.conf` is the initial NGinx configuration that is used before
   any replicas is activated.
   
 * `enact_template.conf` is a template NGinx configuration that the
   `endpoints.sh` uses to rebuild the NGinx configuration each time it
   switches from one replica to the next.

 * `Dockerfile` governs how the enact-image is built.


In the running enact-nginx container, these files are located as
follows:

 - `/enact/' contains the scripts and the list of endpoints.
 - '/etc/nginx/' contains the configuration of the NGinx server.


# How Does it Work?
A shell script is embedded (see the Dockerfile). This script comes
along with a list of endpoints (i.e., `endpoints.dat`).

The main entry point is the script `endpoints.sh`, which offers the
following commands:

```console
$ bash endpoints.sh whatever
Unknown command 'whatever'.
Usage: endpoints.sh [CMD] [OPTIONS]

where CMD is one the following: 
  activate [ENDPOINT]
      activate the given endpoint;

  discard [ENDPOINT]
      remove the given endpoint;

  failure-of [ENDPOINT]
      react to a failure of the given endpoint;

  initialize [PROXY_PORT] [ACTIVE_ENDPOINT]
      initialize the proxy listening on a given PROXY_PORT and forwarding
      to the given ACTIVE_ENDPOINT;

  register [ENDPOINT]
      register a new endpoint in the list;

  repair-of [ENDPOINT]
      react to the repair of the given endpoint;

  show                   how the list of known endpoints;

  show-active            show the endpoint currently used;

  show-backup            show the backup endpoint in the list;

  show-proxy             show the configuration of the proxy;

  show-upgrade           show the possible upgrade endpoint
```

This script includes a `DEBUG` variable, which you can set to "yes",
to avoid triggering NGinx: It simply outputs the modified NGinx
configuration on the command line instead.

The GeneSIS server uses this scripts as follows:

1. It create a container, called proxy, out of the `fchauvel/enact-nginx` image
   available on DockerHub. At this stage NGinx serves some dummy
   content. This can be changed in the ǹginx.conf` file.
   
2. For each replica it creates, GeneSIS logs in the proxy container
   and register the endpoint of this replica, using the command:
   ```console
   $ bash endpoints.sh register a_component-1:4567
   ```
   
   This steps add the endpoint `a_component-1:4567` to the
   `endpoints.dat` file and creates a new CRON task, that triggers the
   watchdog script every minute or so. This can be changed in the
   `endpoints.sh` file.
   
3. Once all replicas are up and running, GeneSIS logs in the proxy
   container and runs the command:
   ```console
   $ bash endpoints.sh initialize 5000 a_component-1:4567
   ```
   This configure the NGinx proxy to listen on the port 5000 and to
   redirect TCP traffic to the machine a_component-1:4567. Note that
   the container a_component-1 is connected to a docker network,
   together with the proxy container, but only the proxy container is
   available from the outside.


# How to Update the Image?
This image also comes with a script to automate updating the
enact-nginx image. Currently the image is available on DockerHub at
`fchauvel/enact-nginx`. You must have Docker installed on the machine
where you run this command.

```console
$ bash publish.sh
```

The script will cleans up `endpoints.dat`, ensure the `endpoints.sh`
is not in DEBUG mode, rebuilt the Docker image and finally upload the
newly built image on DockerHub.

> **Note** If you publish this image in a different repository (than
> `fchauvel/enact-nginx`), you must update GeneSIS accordingly (i.e.,
> `src/engine/engine.js`)

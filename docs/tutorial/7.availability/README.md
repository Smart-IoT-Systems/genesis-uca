# Availability Mechanisms

GeneSIS can improve availability by deploying multiple replicas of a
given component (or service) and ensuring that at least one replicas
is up and running. Availability refers to the capacity of the system
to mask or repair fault in order to minimise the total outage service
duration.

GeneSIS offers two main implementations of these availability
strategies: Either using Docker Swarm, or using builtin mechanisms.

These availability mechanisms are activated when a component includes
an "availability policy", as illustrated below, with the key
`availability`.

```json
components: [
    {
        "name": "my_sample_component",
        "properties": [],
        "availability": {
            "strategy": "Docker Swarm",
            "healthCheck": "Some shell code goes here"
            "replicaCount": 2,
            "zeroDownTime": true,
            "exposedPort": 5000
        },
    },
]
```

> **NOTE** Availability policies are only supported for Docker and SSH
> components.

This availability specifies 5 elements:

1.   `strategy`, decides how the availability mechanisms will be
     implemented. It can take two values, either "Builtin" or "Docker
     Swarm".

2.   `healthCheck` contains a shell script (whose code must be
     escaped),which check the availability of components'
     replicas. This health check must accept the replica endpoint as a
     parameter and return 0 if the endpoint is OK, and another number
     otherwise. For instance, one could use the following Bash script:

     ```bash
     #!/bin/bash

     if [ $# -ne 1 ]
     then
        echo "Error: Wrong number of arguments."
        echo "Usage: healthcheck.sh [ENDPOINT]"
        exit 2
    fi

    ENDPOINT="${1}"

    response=$(curl --write-out '%{http_code}' --silent --output /dev/null "${ENDPOINT}")
    if [ "${response}" != 200 ]
    then
        exit 1
    fi
    ```

3.   `replicaCount` specifies how many replicas of the component
     should exist at any given time. It must be a positive integer
     value.

4.   `zeroDownTime` specifies whether GeneSIS must preserve a running
     replicas during updates, in order to ensure service
     continuity. When `zeroDownTime`is true, GeneSIS first deploys new
     replicas, before it terminates existing ones.

5.   `exposePort` specifies the port that GeneSIS will use to
     communicate with the replicas. This port is also the one used to
     access the services.

## Example 1: Replicated Docker Component Using Docker Swarm

In this example we replicate a simple REST service, using Docker
Swarm. Note that this component could also be deployed using the
Builtin implementation strategy.


Our component is defined by its Docker resource, which points to the
Image `fchauvel/test-app:v1.0`, available on DockerHub. Details about
this image are given in appendix.

We show below how this component shows up in the associated deployment
model.

```json
{
    "_type": "/internal",
    "name": "a_component",
    "properties": [],
    "version": "0.0.1",
    "id": "ccf144c1-a193-4252-9751-4cf11a4fa2e0",
    "availability": {
        "strategy": "Docker Swarm",
        "healthCheck": "#!/bin/bash \n\nif [ $# -ne 1 ] \n then \n    echo \"Error: Wrong number of arguments.\" \n    echo \"Usage: healthcheck.sh [ENDPOINT]\"  \n    exit 2 \nfi \n\nENDPOINT=\"${1}\" \n\nresponse=$(curl --write-out '%{http_code}' --silent --output /dev/null \"${ENDPOINT}\") \nif [ \"${response}\" != 200 ] \nthen \n    exit 1 \nfi\n",
        "replicaCount": 2,
        "zeroDownTime": true,
        "exposedPort": 5000
    },
    "docker_resource": {
        "name": "4d4cabc8-dbb9-4536-aa9a-5fcb13fdc47a",
        "image": "fchauvel/test-app:v1.0",
        "command": "cd test-app && bash start.sh",
        "links": [],
        "port_bindings": {
            "5000": "5000"
        },
        "devices": {
        "PathOnHost": "",
        "PathInContainer": "",
        "CgroupPermissions": "rwm"
    }
}
```

### Initial Deployment

Once we trigger the deployment using GeneSIS, we can connect to our Docker host, as list the Swarm services available:

```shell
$ docker service ls
ID                  NAME                MODE                REPLICAS            IMAGE               PORTS
qnijwmupk05j        a_component         replicated          2/2                 fchauvel/test-app:v1.0
```

### Releasing a New Version

We can now return to our deployment model and update the Docker image
from which our component is deployed, say from `test-app:v1.0` to
`test-app:v2.0`.

Now we can use GeneSIS to redeploy our component, by selecting again
`Deployment`>`Deploy model from editor`.

If we return right away to our host and check the list of available
services, we still see our service named `a_component` but it has been
redeployed from the version of our image.

```shell
$ docker service ls
ID                  NAME                MODE                REPLICAS            IMAGE               PORTS
qnijwmupk05j        a_component         replicated          4/2                 fchauvel/test-app:v2.0
```

Note only did the image change, but the number of replicas has
increases, because Docker has provisioned the new container (from the
image v2.0) aside of the old version and waits a bit before to take
down the older one.


### Fault-tolerance: Masking Replica's Failure

To simulate the failure of a replica, let's connect to the host, and
manually stop one of the container underlying the replicas.

Once GeneSIS has deployed our application on top of Docker Swarm, we
can connect to our host and check that a Swarm service has been
provisioned and that two containers (the replicas) are indeed up and
running.

```console
root@debian:~# docker service ls
ID             NAME          MODE         REPLICAS   IMAGE                    PORTS
whcmxi7trg6i   a_component   replicated   2/2        fchauvel/test-app:v1.0   *:5000->5000/tcp
root@debian:~# docker ps -a
CONTAINER ID   IMAGE                    COMMAND                  CREATED          STATUS          PORTS     NAMES
b99c328f0ce6   fchauvel/test-app:v1.0   "/bin/sh -c 'python …"   24 seconds ago   Up 22 seconds             a_component.1.1t3no1m24g941u2uzc4omm31c
fdcc5e632442   fchauvel/test-app:v1.0   "/bin/sh -c 'python …"   24 seconds ago   Up 22 seconds             a_component.2.g8gw3qzjcr8b0buv73v6ommm7
```

To test the availability mechanisms, we can now stop one these
containers `docker stop` command (say the one with ID b99c32), and
then see whether the service status does change. When we list again
the container, we can see that a new container has been provisioned
(with ID 1a08b2b4f5a0).

```console
root@debian:~# docker stop b99c32
b99c32
root@debian:~# docker service ls
ID             NAME          MODE         REPLICAS   IMAGE                    PORTS
whcmxi7trg6i   a_component   replicated   1/2        fchauvel/test-app:v1.0   *:5000->5000/tcp
root@debian:~# docker service ls
ID             NAME          MODE         REPLICAS   IMAGE                    PORTS
whcmxi7trg6i   a_component   replicated   2/2        fchauvel/test-app:v1.0   *:5000->5000/tcp
root@debian:~# docker ps -a
CONTAINER ID   IMAGE                    COMMAND                  CREATED          STATUS                        PORTS     NAMES
1a08b2b4f5a0   fchauvel/test-app:v1.0   "/bin/sh -c 'python …"   12 seconds ago   Up 7 seconds                            a_component.1.vvqjpmkadsutgqfg7i04k8qqw
b99c328f0ce6   fchauvel/test-app:v1.0   "/bin/sh -c 'python …"   43 hours ago     Exited (137) 13 seconds ago             a_component.1.1t3no1m24g941u2uzc4omm31c
fdcc5e632442   fchauvel/test-app:v1.0   "/bin/sh -c 'python …"   43 hours ago     Up 43 hours                             a_component.2.g8gw3qzjcr8b0buv73v6ommm7
```


## Using Builtin Mechanisms

In this example we replicate and SSH component using the Builtin
strategy.

Our SSH component refers to the sources of our test-app component. It
comes with several bash scripts that can be used to install and start
it on Debian host.

In the following JSON excerpt, we attach an SSH resource to our
component, where we define the Shell commands needed to download,
install and start the component. Note that the host component (not
shown below) is a simple virtual machine (running Debian 10), and
accessible through SSH.

```json
    "components": [
      {
        "_type": "/internal",
        "name": "a_component",
        "properties": [],
        "version": "0.0.1",
        "id": "ccf144c1-a193-4252-9751-4cf11a4fa2e0",
        "availability": {
          "strategy": "Builtin",
          "healthCheck": "#!/bin/bash \n\nif [ $# -ne 1 ] \n then \n    echo \"Error: Wrong number of arguments.\" \n    echo \"Usage: healthcheck.sh [ENDPOINT]\"  \n    exit 2 \nfi \n\nENDPOINT=\"${1}\" \n\nresponse=$(curl --write-out '%{http_code}' --silent --output /dev/null \"${ENDPOINT}\") \nif [ \"${response}\" != 200 ] \nthen \n    exit 1 \nfi\n",
            "replicaCount": 2,
            "zeroDownTime": true,
            "exposedPort": 5000
        },
        "ssh_resource": {
          "name": "c3370663-cc7a-4e7b-9094-6d2fdf8ce029",
          "downloadCommand": "apt-get update && apt-get install --yes git && git clone https://github.com/fchauvel/test-app.git",
          "installCommand": "cd test-app && bash install.sh",
          "startCommand": "cd test-app && bash start.sh",
          "configureCommand": "",
          "stopCommand": "",
          "uploadCommand": [],
          "credentials": {
            "username": "root",
            "password": "************",
            "sshkey": "",
            "agent": ""
          }
        },
```


### Initial Deployment

Initially, our host is only available through SSH, and Docker is not
available there as we shown below.

```shell
$ ssh root@192.168.1.162
root@192.168.1.162's password:
Linux debian 4.19.0-12-amd64 #1 SMP Debian 4.19.152-1 (2020-10-18) x86_64

The programs included with the Debian GNU/Linux system are free software;
the exact distribution terms for each program are described in the
individual files in /usr/share/doc/*/copyright.

Debian GNU/Linux comes with ABSOLUTELY NO WARRANTY, to the extent
permitted by applicable law.
Last login: Tue Nov  3 15:55:04 2020
root@debian:~# docker
-bash: docker: command not found
root@debian:~#
```

When we trigger the deployment, GeneSIS first connects to our host
and install Docker. This is needed because running multiple instance
of the same service on the same machine requires isolation mechanisms
which container technologies provide. Once Docker is ready, GeneSIS
create a new container, connects to this container and execute all the
command specified in the SSH resource. GeneSIS then saved this
container as a new Docker image.

```
ssh root@192.168.1.162
root@192.168.1.162's password:
root@debian:~# docker images
REPOSITORY              TAG       IMAGE ID       CREATED         SIZE
a_component-livebuilt   latest    82be8f1422be   3 minutes ago   246MB
fchauvel/enact-nginx    latest    88c27354e4d5   3 days ago      153MB
debian                  10-slim   cbd3a5bf0324   10 days ago     69.2MB
```

When we connect to the host (using SSH), we see here that GeneSIS has
installed and configure Docker in remote mode. GeneSIS has also
created a new image a new image for our component named
`a_component-livebuilt`, derived from the Image
`debian:10-slim`. GeneSIS has also pulled the `fchauvel/enact-nginx`,
which it uses to instantiate a proxy in front of the replicas.

If we now look at the containers running on our host, we see three: two
replicas (instances of the `a_component-livebuilt` image) and the
proxy, instance of the `fchauvel/enact-proxy` image);

```console
root@debian:~# docker ps -a
CONTAINER ID   IMAGE                          COMMAND                  CREATED              STATUS          PORTS                            NAMES
ffbbc8bfb493   fchauvel/enact-nginx:latest    "/docker-entrypoint.…"   45 seconds ago       Up 44 seconds   80/tcp, 0.0.0.0:5000->5000/tcp   a_component-proxy
699693af5831   a_component-livebuilt:latest   "/bin/bash -c 'cd te…"   59 seconds ago       Up 58 seconds                                    a_component-2
9d2f154e19c0   a_component-livebuilt:latest   "/bin/bash -c 'cd te…"   About a minute ago   Up 59 seconds                                    a_component-1
```

### Releasing a New Version
In this section, we look at how these availability mechanisms cope
with updates. There is two options:

1. **Regular Update**, where GeneSIS stops all existing replicas and
   recreates new ones. The service is thus unavailable for a short
   time, while the new replicas is starting up.

1. **Zero Down Time**, where GeneSIS first creates new replicas and
   then stop the older ones. This ensures service continuity, as there
   is always at least one replica running.


We show below how GeneSIS behaves when updating an SSH component to
guarantee zero down time.

We first specify our availability policy in our deployment model,
attaching it to the component of interest, in our case the one
named`a_component`.


```json
"availability": {
    "strategy": "Builtin",
    "healthCheck": "#!/bin/bash \n\nif [ $# -ne 1 ] \n then \n    echo \"Error: Wrong number of arguments.\" \n    echo \"Usage: healthcheck.sh [ENDPOINT]\"  \n    exit 2 \nfi \n\nENDPOINT=\"${1}\" \n\nresponse=$(curl --write-out '%{http_code}' --silent --output /dev/null \"${ENDPOINT}\") \nif [ \"${response}\" != 200 ] \nthen \n    exit 1 \nfi\n",
    "replicaCount": 2,
    "zeroDownTime": true,
    "exposedPort": 5000
},
```

Once GeneSIS has deployed our application, we can login on the host
using SSH and check that 3 Docker containers are now running: Two
replicas of our service and the NGinx proxy.

```console
ssh root@192.168.1.162
root@192.168.1.162's password:
root@debian:~# docker ps -a
CONTAINER ID   IMAGE                          COMMAND                  CREATED              STATUS              PORTS                            NAMES
b41891e8e973   fchauvel/enact-nginx:latest    "/docker-entrypoint.…"   About a minute ago   Up About a minute   80/tcp, 0.0.0.0:5000->5000/tcp   a_component-proxy
1041c31c215b   a_component-livebuilt:latest   "/bin/bash -c 'cd te…"   About a minute ago   Up About a minute                                    a_component-2
43f590859a50   a_component-livebuilt:latest   "/bin/bash -c 'cd te…"   About a minute ago   Up About a minute                                    a_component-1
```

Now, let's modify our deployment model, say for instance to increase
the number of replica from 2 to 4. When we redeploy our application, we
can see that GeneSIS first provisions the 4 new replica instance before to
terminate the existing one. As we show below, there are now 7
containers running on our host: the NGinx proxy, 2 old containers and
4 new containers.

```
root@debian:~# docker ps -a
CONTAINER ID   IMAGE                          COMMAND                  CREATED          STATUS         PORTS                            NAMES
cf558cfbe26d   a_component-livebuilt:latest   "/bin/bash -c 'cd te…"   8 seconds ago    Up 7 seconds                                    a_component-6
2cb5c01a82ae   a_component-livebuilt:latest   "/bin/bash -c 'cd te…"   8 seconds ago    Up 8 seconds                                    a_component-5
f0fc795a0c03   a_component-livebuilt:latest   "/bin/bash -c 'cd te…"   9 seconds ago    Up 8 seconds                                    a_component-4
5f121c4689c2   a_component-livebuilt:latest   "/bin/bash -c 'cd te…"   10 seconds ago   Up 9 seconds                                    a_component-3
b41891e8e973   fchauvel/enact-nginx:latest    "/docker-entrypoint.…"   2 minutes ago    Up 2 minutes   80/tcp, 0.0.0.0:5000->5000/tcp   a_component-proxy
1041c31c215b   a_component-livebuilt:latest   "/bin/bash -c 'cd te…"   2 minutes ago    Up 2 minutes                                    a_component-2
43f590859a50   a_component-livebuilt:latest   "/bin/bash -c 'cd te…"   2 minutes ago    Up 2 minutes                                    a_component-1
```

If we wait a few more seconds, we see that GeneSIS eventually stops
and removes these older containers, and there only remains the NGinx
proxy and its four service replicas (numbered 3, 4, 5, and 6).

```console
root@debian:~# docker ps -a
CONTAINER ID   IMAGE                          COMMAND                  CREATED          STATUS          PORTS                            NAMES
cf558cfbe26d   a_component-livebuilt:latest   "/bin/bash -c 'cd te…"   33 seconds ago   Up 32 seconds                                    a_component-6
2cb5c01a82ae   a_component-livebuilt:latest   "/bin/bash -c 'cd te…"   33 seconds ago   Up 32 seconds                                    a_component-5
f0fc795a0c03   a_component-livebuilt:latest   "/bin/bash -c 'cd te…"   34 seconds ago   Up 33 seconds                                    a_component-4
5f121c4689c2   a_component-livebuilt:latest   "/bin/bash -c 'cd te…"   35 seconds ago   Up 34 seconds                                    a_component-3
b41891e8e973   fchauvel/enact-nginx:latest    "/docker-entrypoint.…"   2 minutes ago    Up 2 minutes    80/tcp, 0.0.0.0:5000->5000/tcp   a_component-proxy
```


### Fault-tolerance: Killing One Replica

Let us now show how the availability mechanisms that GeneSIS deploys
can help mask failure to the user. To do so, we manually stop one
replica and see how the watchdogs pick up the failure and
automatically provision a replacement.

As before, once we have deployed our test application, we can now
connect to the host, and check that GeneSIS has created three
containers: An instance of the NGinx proxy named `a_component-proxy`,
and two instances of our test application named `a_component-1` and
`a_component-2` respectively.

```shell
$ ssh root@192.168.1.162
root@192.168.1.162's password:

root@debian:~# docker ps -a
CONTAINER ID   IMAGE                          COMMAND                  CREATED         STATUS         PORTS                            NAMES
ffbbc8bfb493   fchauvel/enact-nginx:latest    "/docker-entrypoint.…"   6 minutes ago   Up 5 minutes   80/tcp, 0.0.0.0:5000->5000/tcp   a_component-proxy
699693af5831   a_component-livebuilt:latest   "/bin/bash -c 'cd te…"   6 minutes ago   Up 6 minutes                                    a_component-2
9d2f154e19c0   a_component-livebuilt:latest   "/bin/bash -c 'cd te…"   6 minutes ago   Up 6 minutes                                    a_component-1
```

We can now connect to the proxy container and look at the logs of the
watchdogs (CRON tasks), which periodically check whether the replicas
are still up and running. As you can see below, each watchdog (one per
replica) creates a log file `watchdog_a_component-X.log`, where we can
see the result of the health checks.

```shell
root@debian:~# docker exec -it a_component-proxy /bin/bash
root@ffbbc8bfb493:/enact# ls -l
ls -l
total 44
-rw-r--r-- 1 root root   144 Feb 19 20:35 docker.sh
-rw-r--r-- 1 root root   155 Feb 19 20:36 endpoints.dat
-rw-r--r-- 1 root root 11690 Feb 16 14:13 endpoints.sh
-rw-r--r-- 1 1000 1000   302 Feb 19 20:35 healthcheck.sh
-rw-r--r-- 1 root root  6667 Feb 16 14:12 restart.sh
-rwxr-xr-x 1 root root  1827 Feb 16 08:55 watchdog.sh
-rw-r--r-- 1 root root   742 Feb 19 20:42 watchdog_a_component-1.log
-rw-r--r-- 1 root root   763 Feb 19 20:42 watchdog_a_component-2.log
root@ffbbc8bfb493:/enact# cat watchdog_a_component-1.log
cat watchdog_a_component-1.log
Fri Feb 19 20:36:01 UTC 2021 INFO: Heath check for 'a_component-1:5000' returned '0'
Fri Feb 19 20:36:01 UTC 2021 INFO: Endpoint 'a_component-1:5000' is back!
Endpoint already known.
Endpoint 'a_component-2:5000' is already active!
Fri Feb 19 20:37:01 UTC 2021 INFO: Heath check for 'a_component-1:5000' returned '0'
Fri Feb 19 20:38:01 UTC 2021 INFO: Heath check for 'a_component-1:5000' returned '0'
Fri Feb 19 20:39:01 UTC 2021 INFO: Heath check for 'a_component-1:5000' returned '0'
Fri Feb 19 20:40:01 UTC 2021 INFO: Heath check for 'a_component-1:5000' returned '0'
Fri Feb 19 20:41:01 UTC 2021 INFO: Heath check for 'a_component-1:5000' returned '0'
Fri Feb 19 20:42:01 UTC 2021 INFO: Heath check for 'a_component-1:5000' returned '0'
```

Let's now open a second SSH connection to our host, and from there,
let's stop one of the replica container, say `a_component-1`. Once we
issue he command `docker stop a_component-1`, we see that the
container is indeed marked as 'Exited'.

```shell
$ ssh root@192.168.1.162
root@192.168.1.162's password:
root@debian:~# docker ps -a
CONTAINER ID   IMAGE                          COMMAND                  CREATED              STATUS          PORTS                            NAMES
ffbbc8bfb493   fchauvel/enact-nginx:latest    "/docker-entrypoint.…"   45 seconds ago       Up 44 seconds   80/tcp, 0.0.0.0:5000->5000/tcp   a_component-proxy
699693af5831   a_component-livebuilt:latest   "/bin/bash -c 'cd te…"   59 seconds ago       Up 58 seconds                                    a_component-2
9d2f154e19c0   a_component-livebuilt:latest   "/bin/bash -c 'cd te…"   About a minute ago   Up 59 seconds                                    a_component-1
root@debian:~# docker stop a_component-1
a_component-1
root@debian:~# docker ps -a
CONTAINER ID   IMAGE                          COMMAND                  CREATED         STATUS                        PORTS                            NAMES
ffbbc8bfb493   fchauvel/enact-nginx:latest    "/docker-entrypoint.…"   8 minutes ago   Up 8 minutes                  80/tcp, 0.0.0.0:5000->5000/tcp   a_component-proxy
699693af5831   a_component-livebuilt:latest   "/bin/bash -c 'cd te…"   8 minutes ago   Up 8 minutes                                                   a_component-2
9d2f154e19c0   a_component-livebuilt:latest   "/bin/bash -c 'cd te…"   8 minutes ago   Exited (137) 40 seconds ago                                    a_component-1

```

Let's wait about 1 minute&mdash;that's the time for the watchdog to
detect the failure. If we now return to our first SSH session, from
which we are connected to the NGinx proxy, we can see in the log
file that the watchdog has detected the failure, remove the failed
container and provisioned a replacement.

```shell
root@ffbbc8bfb493:/enact# cat watchdog_a_component-1.log
cat watchdog_a_component-1.log
Fri Feb 19 20:36:01 UTC 2021 INFO: Heath check for 'a_component-1:5000' returned '0'
Fri Feb 19 20:36:01 UTC 2021 INFO: Endpoint 'a_component-1:5000' is back!
Endpoint already known.
Endpoint 'a_component-2:5000' is already active!
Fri Feb 19 20:37:01 UTC 2021 INFO: Heath check for 'a_component-1:5000' returned '0'
Fri Feb 19 20:38:01 UTC 2021 INFO: Heath check for 'a_component-1:5000' returned '0'
Fri Feb 19 20:39:01 UTC 2021 INFO: Heath check for 'a_component-1:5000' returned '0'
Fri Feb 19 20:40:01 UTC 2021 INFO: Heath check for 'a_component-1:5000' returned '0'
Fri Feb 19 20:41:01 UTC 2021 INFO: Heath check for 'a_component-1:5000' returned '0'
Fri Feb 19 20:42:01 UTC 2021 INFO: Heath check for 'a_component-1:5000' returned '0'
Fri Feb 19 20:43:01 UTC 2021 INFO: Heath check for 'a_component-1:5000' returned '0'
Fri Feb 19 20:44:01 UTC 2021 INFO: Heath check for 'a_component-1:5000' returned '1'
Fri Feb 19 20:44:01 UTC 2021 INFO: Endpoint 'a_component-1:5000' has failed!
Configuration loaded from '/enact/docker.sh':
 - Host: '192.168.1.162:2376'
 - Image: 'a_component-livebuilt:latest'
 - Command: 'cd test-app && bash start.sh'
 - Network: 'GeneSIS-a_component'
Container 'a_component-1' stopped.
Container 'a_component-1' removed.
New container 'a_component-1' from image 'a_component-livebuilt:latest' created!
ID: "73684efd1b7f73873a0f5238bb350c4090f552cc9e19f14e0915ee044d36be53"
Container 'a_component-1' connected to 'GeneSIS-a_component'
Container 'a_component-1' started!
root@ffbbc8bfb493:/enact#
```

Finally, on our second SSH connection to our host, we can check that
indeed, three containers are still up and running.

```shell
root@debian:~# docker ps -a
CONTAINER ID   IMAGE                          COMMAND                  CREATED          STATUS          PORTS                            NAMES
73684efd1b7f   a_component-livebuilt:latest   "/bin/bash -c 'cd te…"   16 seconds ago   Up 15 seconds                                    a_component-1
ffbbc8bfb493   fchauvel/enact-nginx:latest    "/docker-entrypoint.…"   8 minutes ago    Up 8 minutes    80/tcp, 0.0.0.0:5000->5000/tcp   a_component-proxy
699693af5831   a_component-livebuilt:latest   "/bin/bash -c 'cd te…"   8 minutes ago    Up 8 minutes                                     a_component-2
root@debian:~#
```

## Appendix: Building the `test-app` Image

Let us introduce a simple service as a running example of service that
can benefit from availability mechanisms. The source code is
[available on Github]/(https://github.com/fchauvel/test-app).

Our service is a single
Python application, using Flask to expose a single endpoint where it
shows its version number. Here is the Python code that we placed in
the file `app.py`.

```python
import os
import flask;

VERSION = os.environ["FC_APP_VERSION"] or "1.0"

app = flask.Flask(__name__)
app.config["DEBUG"] = True


@app.route('/', methods=['GET'])
def home():
    return "<h1>Service v%s</h1>" % VERSION

app.run(host="0.0.0.0")
```

This app comes along with a simple `requirements.txt` file, that
specifies its Python dependencies, here, flask basically.

```
flask
```

We can now bundle this application into a Docker image using the
following Dockerfile:


```dockerfile
FROM python:3.8-buster

ARG APP_VERSION=1.0

ENV FC_APP_VERSION=${APP_VERSION}

RUN apt-get --yes  update

WORKDIR ./enact

COPY ./requirements.txt ./requirements.txt
COPY ./app.py ./app.py

RUN pip install -r requirements.txt

CMD python app.py
```

Now we can build the Docker image using the command:

```shell
$ docker build --build-arg APP_VERSION=1.0 -t test-app:v1.0 .
```

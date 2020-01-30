# Installation

> GeneSIS requires the following components to be available:
>  - Node.js v7
>  - NPM v4
>  - Java v8


## Install GeneSIS from the Sources

If you want to run the latest version of code from git, you can
directly install GeneSIS from its sources, as follows:

1.  Clone the code:

    ```console
    $ git clone https://gitlab.com/enact/GeneSIS.git
    $ cd GeneSIS
    ```

2.  Install the dependencies using NPM

    ```console
    $ cd src
    $ npm install .
    ```

3.  Start GeneSIS. Once it is started you can access it at the URL
    `http://uyour_ip:8880`

    ```console
    $ npm start
    ```

## Installation from the public Docker image:

GeneSIS is available, pre-packaged with all its dependencies, as a
Docker image. Provided you have Docker up and running, you can proceed
as follows:

1.  Pull the image:

    ```console
    $ docker pull enactproject/genesis:latest
    ```

2.  Run the docker container (Depending on how you plan to use
    GeneSIS, remember to open the proper ports,
    cf. https://docs.docker.com/engine/reference/run/). Once GeneSIS
    started, you can access the GeneSIS web interface from the URL:
    `http://<your_ip>:8880`, where `your_ip` stands for your IP
    address.


## Configuring Remote Docker

If you plan to use GeneSIS to deploy docker containers, you must
activate the Docker Remote API on the target host. For instance, on a
Raspberry Pi, you can install docker using:

```console
$ curl -sSL https://get.docker.com | sh
```

Then configure it to accept remote requests as follows:
1.  Create a file called:

    ```console
    $ touch /etc/systemd/system/docker.service.d/startup_options.conf
    ```

1.  Add into this new file:

    ```ini
    # /etc/systemd/system/docker.service.d/override.conf
    [Service]
    ExecStart=
    ExecStart=/usr/bin/dockerd -H tcp://0.0.0.0:2376 -H unix:///var/run/docker.sock
    ```

3.  Reload the unit files, and the restart the Docker daemon as follows:

    ```shell
    $ sudo systemctl daemon-reload
    $ sudo systemctl restart docker.service
    ```

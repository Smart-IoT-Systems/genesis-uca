FROM ubuntu:20.04

USER root

ARG DEBIAN_FRONTEND=noninteractive

RUN apt-get update
RUN apt-get install -y curl
RUN apt-get install -y git
RUN apt-get install -y nodejs
RUN apt-get install -y npm
RUN apt-get install -y openjdk-8-jre

RUN curl https://downloads.arduino.cc/arduino-1.8.5-linux64.tar.xz | tar xJC /

RUN npm install npm@latest -g

VOLUME ["/data"]

RUN npm install -g --unsafe-perm node-red

WORKDIR /root/.node-red

ENV PATH="${PATH}:/arduino-1.8.5"
ARG NODE_TLS_REJECT_UNAUTHORIZED=0
RUN npm install \
        node-red-node-serialport \
        git+https://gitlab.com/enact/node-red-contrib-thingml-preparedeploy-docker \
        git+https://gitlab.com/enact/node-red-contrib-thingml-compiler \
        git+https://gitlab.com/enact/node-red-contrib-docker-compose \
        git+https://gitlab.com/enact/node-red-contrib-arduino \
        git+https://github.com/FIWARE/node-red-contrib-FIWARE_official.git

RUN apt-get install -y libtinfo5
RUN curl -L https://www.dropbox.com/s/tibyn4sd8kfya02/ThingML2CLI.jar?dl=0  --output /root/.node-red/node_modules/node-red-contrib-thingml-compiler/lib/thingml/ThingML2CLI.jar
ENTRYPOINT node-red
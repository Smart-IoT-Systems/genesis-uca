FROM arm32v7/ubuntu:18.04

USER root

ARG DEBIAN_FRONTEND=noninteractive

RUN apt-get update
RUN apt-get install -y nodejs
RUN apt-get install -y npm
RUN apt-get install -y openjdk-8-jre
RUN apt-get install -y curl
RUN apt-get install -y git

RUN npm config set registry http://registry.npmjs.org/

RUN npm install -g n
RUN n stable
RUN n 11.8.0

RUN npm install npm@latest -g

RUN apt-get clean

RUN git clone https://gitlab.com/enact/GeneSIS.git
WORKDIR "/GeneSIS/src"
RUN npm install

RUN apt-get purge -y --auto-remove git

ENTRYPOINT npm start

FROM node:12.9.0-buster

RUN apt-get update

RUN apt install -y openjdk-11-jdk

RUN git clone https://gitlab.com/enact/GeneSIS.git
WORKDIR "/GeneSIS"
RUN npm install

ENTRYPOINT npm start


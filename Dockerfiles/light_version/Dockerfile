FROM mhart/alpine-node:12

WORKDIR /genesis
COPY . .
WORKDIR /genesis/src

# If you have native dependencies, you'll need extra tools
# RUN apk add --no-cache make gcc g++ python

RUN npm install --production

ENTRYPOINT npm start
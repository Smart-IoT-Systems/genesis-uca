# Deploying CouchDB with Ansible.



## Start GeneSIS:

First, let’s start GeneSIS by using the following command in the root folder of GeneSIS:

        npm start

You should see the following message:

        > GeneSIS@0.0.1 start /Users/ferrynico/Documents/Code/GeneSIS-gitlab/GeneSIS-v2/GeneSIS
        > concurrently "nodemon ./app.js" "webpack-dev-server "

        [0] [nodemon] 1.18.11
        [0] [nodemon] to restart at any time, enter `rs`
        [0] [nodemon] watching: *.*
        [0] [nodemon] starting `node ./app.js`
        [0] 2019-05-09T07:28:29.357Z - [info]: Engine started!
        [0] 2019-05-09T07:28:29.452Z - [info]: PlantUML diagram generator started on port: 8080
        [0] 2019-05-09T07:28:29.469Z - [info]: GeneSIS Engine API started on 8000
        [0] 2019-05-09T07:28:29.471Z - [info]: MQTT websocket server listening on port 9001
        [0] 2019-05-09T07:28:29.639Z - [info]: New MQTT client mqttjs_52139cc4
        [0] 2019-05-09T07:28:29.701Z - [info]: subscribe from client [object Object],[object Object] from mqttjs_52139cc4
        [1] ℹ ｢wds｣: Project is running at http://localhost:8880/
        [1] ℹ ｢wds｣: webpack output is served from http://127.0.0.1:8880/dist/
        [1] ℹ ｢wds｣: Content not from webpack is served from /Users/ferrynico/Documents/Code/GeneSIS-gitlab/GeneSIS-v2/GeneSIS/public/
        [0] 2019-05-09T07:28:30.489Z - [info]: New MQTT client mqttjs_75f09b96

Once GeneSIS started, you can access the GeneSIS editor at the following address:

        http://127.0.0.1:8880

## Specifying the deployment model

## Deploy



That's all folks!

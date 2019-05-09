# Deploying Node-RED on a Docker engine

In this example we will simply deploy a Node-Red container via Docker on the machine running GeneSIS. 

Here, we assume that (i) a Docker engine is up on the machine running GeneSIS and with the Docker Remote engine accessible, and (ii) GeneSIS is properly installed on the machine.

Instructions on how to make the Docker remote engine accessible can be found [here](../../../)

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
        [0] 2019-04-17T10:39:51.508Z - [info]: Engine started!
        [0] 2019-04-17T10:39:51.524Z - [info]: PlantUML diagram generator started on port: 8080
        [0] 2019-04-17T10:39:51.527Z - [info]: GeneSIS Engine API started on 8000
        [0] 2019-04-17T10:39:51.528Z - [info]: MQTT websocket server listening on port 9001
        [1] ℹ ｢wds｣: Project is running at http://localhost:8880/
        [1] ℹ ｢wds｣: webpack output is served from http://127.0.0.1:8880/dist/
        [1] ℹ ｢wds｣: Content not from webpack is served from /Users/ferrynico/Documents/Code/GeneSIS-gitlab/GeneSIS-v2/GeneSIS/public/
        [1] ℹ ｢wdm｣: Hash: 6cedc4f9ed78f33b2677

Once GeneSIS started, you can access the GeneSIS editor at the following address:

        http://127.0.0.1:8880

## Specifying the deployment model

We can now start specifying our deployment model. We first start by the high level architecture of our deployment before we dig into the configuration of the different components that form our deployment model. 

We need first to remember that the GeneSIS Modelling language is inspired by component-based approaches. Deployment models can thus be regarded as assemblies of components. 

In this example, our deployment model will be composed of two components: 
* a _SoftwareComponent_ (i.e., the Node-RED container that will be deployed by GeneSIS) and more precisely an InternalComponent as its deployment life-cycle will be managed by GeneSIS.
* an _InfrastructureComponent_ (i.e., the host on top of which we will deploy our SoftwareComponent, in our case a DockerEngine).

First, we start by creating the InternalComponent by clicking on 'Edit > Software Component > Internal Component > Node-RED' as depicted in the Figure below.

![alt text](docs/tutorial/1.nodered_localhost/images/create_component.png "Create Component")

At the current moment we just specify its 'name' and click on the 'OK' button to actually add the component to our deployment model.
A circle should appear in the editor! Please note that you can (i) zoom in/out but using the mouse wheel, (ii) you can move a component by drag and drop, and (iii) you can edit the properties of a component with a right click on it.

We can now create the InfrastructureComponent by clicking on 'Edit > InfrastructureComponent > Docker Host'
At the current moment just specify it 'name' and click on 'OK'. A rectangle should appear!

We will now specify that our InternalComponent will be deployed on our InfrastructureComponent (i.e., Node-RED on Docker). To do so, we need (i) to specify the execution ports of our components and (ii) to create a containement relationship between the two components.
We first specify the provided execution port of the docker host (i.e., my_machine). Right-click on the Docker Host and change the name of the 'provided execution port' property (e.g., offerDocker).
Similarly right-click on the Software component (i.e., nodered) and change the name of the required execution port (e.g., demandDocker).

Then we can add the containment relationship by clicking on 'Edit > Link > Add Containment'.
Select the proper nodes and click on 'add'. The circle should now be contained by the rectangle as depicted in the Figure below.

![alt text](docs/tutorial/1.nodered_localhost/images/containment.png "Containment")

We are now going to configure our two components. 
For our InfrastructureComponent (aka., Docker Host), we need to make sure that the 'IP' property of the component is set to '127.0.0.1'.
We can edit this property of the component by right-clicking on it and modifying the 'IP' field as depicted in the Figure below.

![alt text](docs/tutorial/1.nodered_localhost/images/port_container.png "Set port of the Docker engine")

For editing the InternalComponent (aka., Node-RED), we can use the GeneSIS JSON editor. You can move to the JSON editor view by clicking on the top right button named 'JSONEditor'.
We need to specify the Docker port binding (i.e., how the port of the service running in the Docker container will be accessible from outside).
We can edit the JSON as depicted in the figure below. Typically, Node-RED is exposed using port 1880.

![alt text](docs/tutorial/1.nodered_localhost/images/delete.png "Delete inputs in the JSON")
![alt text](docs/tutorial/1.nodered_localhost/images/port.png "Set port of the Docker binding")

Now it is time to deploy!

## Deploy

Click on 'Deploy > All'.

Once the deployment started, you can observe deployment logs in the console where you started GeneSIS. The deployment process will consist in (i) checking if the Docker engine is reachable (if so the rectangle will turn green), (ii) pulling the Node-RED docker image (whilst this operation is being performed the component will be yellow), and (iii) start the Docker container (At this point the circle should also be green as depicted in the Figure below).

![alt text](docs/tutorial/1.nodered_localhost/images/deployment.png "Successful deployment")

Finally, you can access to your Node-RED by right-clicking on the component in the graph view of the editor and by clicking on the “Go To” button.

## Deploy on a remote Host

So far, we deployed Node-RED on the machine running GeneSIS. But actually, we could have use the same process to deploy Node-RED on a remote host.
To do so, we only change the IP parameter of the InfrastructureComponent (aka., the host) to the IP address of the host where we want to deploy Node-RED.


## Deploy Node-RED and initialize it with a flow

When deploying an instance of the Node-RED runtime you can use the property _nr-flow_ to specify a Node-RED flow that should be deployed once the Node-RED runtime is up.
In this tutorial we can use the following flow.

        [{"id":"6ce6e39a.e6af9c","type":"tab","label":"Flow 1","disabled":false,"info":""},{"id":"86457344.50e6b","type":"inject","z":"6ce6e39a.e6af9c","name":"","topic":"","payload":"","payloadType":"none","repeat":"","crontab":"","once":false,"x":230,"y":180,"wires":[["9a142026.fa47f"]]},{"id":"9a142026.fa47f","type":"function","z":"6ce6e39a.e6af9c","name":"add new layer","func":"msg.payload = {};\nmsg.payload.command = {};\n\nvar u = 'http://{s}.tile.openstreetmap.fr/hot/{z}/{x}/{y}.png';\nvar o = { maxZoom: 19, attribution: '&copy; OpenStreetMap'};\n\nmsg.payload.command.map = {name:\"OSMhot\", url:u, opt:o};\nmsg.payload.command.layer = \"OSMhot\";\n\nreturn msg;","outputs":1,"noerr":0,"x":460,"y":180,"wires":[["c643e022.1816c"]]},{"id":"c643e022.1816c","type":"worldmap","z":"6ce6e39a.e6af9c","name":"","x":790,"y":220,"wires":[]},{"id":"2998e233.4ba64e","type":"function","z":"6ce6e39a.e6af9c","name":"USGS Quake monitor csv re-parse","func":"msg.payload.lat = msg.payload.latitude;\nmsg.payload.lon = msg.payload.longitude;\nmsg.payload.layer = \"earthquake\";\nmsg.payload.name = msg.payload.id;\nmsg.payload.icon = \"globe\";\nmsg.payload.iconColor = \"orange\";\n\ndelete msg.payload.latitude;\ndelete msg.payload.longitude;\t\nreturn msg;","outputs":1,"noerr":0,"x":580,"y":320,"wires":[["c643e022.1816c"]]},{"id":"e72c5732.9fa198","type":"function","z":"6ce6e39a.e6af9c","name":"move and zoom","func":"msg.payload = { command:{layer:\"Esri Terrain\",lat:0,lon:0,zoom:3} };\nreturn msg;","outputs":1,"noerr":0,"x":460,"y":220,"wires":[["c643e022.1816c"]]},{"id":"12317723.589249","type":"csv","z":"6ce6e39a.e6af9c","name":"","sep":",","hdrin":true,"hdrout":"","multi":"one","ret":"\\n","temp":"","x":430,"y":260,"wires":[["2998e233.4ba64e"]]},{"id":"10e5e5f0.8daeaa","type":"inject","z":"6ce6e39a.e6af9c","name":"","topic":"","payload":"","payloadType":"none","repeat":"","crontab":"","once":false,"x":230,"y":220,"wires":[["e72c5732.9fa198"]]},{"id":"b6917d83.d1bac","type":"http request","z":"6ce6e39a.e6af9c","name":"","method":"GET","url":"http://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/2.5_day.csv","x":310,"y":320,"wires":[["12317723.589249"]]},{"id":"3842171.4d487e8","type":"inject","z":"6ce6e39a.e6af9c","name":"Quakes","topic":"","payload":"","payloadType":"none","repeat":"900","crontab":"","once":false,"x":240,"y":260,"wires":[["b6917d83.d1bac"]]}]

This flow leverage a _worldmap_ node that needs to be installed in the Node-RED runtime before the flow can be executed. In GeneSIS, this can be specified via the _packages_ property. In the context of our deployment, this property should be assigned with the following value:

        ["node-red-contrib-web-worldmap"]

The Figure below depicts these two properties

![alt text](docs/tutorial/1.nodered_localhost/images/flow.png "Flow Node-RED")

After deployment, your should see the following flow in your Node-RED editor.

![alt text](docs/tutorial/1.nodered_localhost/images/editor.png "Editor Node-RED")

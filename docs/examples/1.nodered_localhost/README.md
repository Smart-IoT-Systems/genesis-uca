# Deploying Node-RED on a Docker engine

In this example we will simply deploy a Node-Red container via Docker on the machine running GeneSIS. 

Here, we assume that (i) a Docker engine is up on the machine running GeneSIS and with the Docker Remote engine accessible, and (ii) GeneSIS is properly installed on the machine.

Instructions on how to make the Docker remote engine accessible can be found [here](../../../)

## Start GeneSIS:

First, let’s start GeneSIS by using the following command in the root folder of GeneSIS:

        npm start

You should see the following message:

        > GeneSIS@0.0.1 start /Users/ferrynico/Documents/Code/GeneSIS-gitlab/GeneSIS
        > node ./app.js
        
        2019-02-18T09:03:41.393Z - [info]: Engine started!
        2019-02-18T09:03:41.404Z - [info]: PlantUML diagram generator started on port: 8080
        2019-02-18T09:03:41.406Z - [info]: MQTT server listening on port 1883
        2019-02-18T09:03:41.407Z - [info]: Magic happens on port 8880

Once GeneSIS started, you can access the GeneSIS editor at the following address:

        http://127.0.0.1:8880

## Specifying the deployment model

We can now start specifying our deployment model. We first start by the high level architecture of our deployment before we dig into the configuration of the different components that form our deployment model. 

We need first to remember that the GeneSIS Modelling language is inspired by component-based approaches. Deployment models can thus be regarded as assemblies of components. 

In this example, our deployment model will be composed of two components: 
* a _SoftwareComponent_ (i.e., the Node-RED container that will be deployed by GeneSIS) and more precisely an InternalComponent as its deployment life-cycle will be managed by GeneSIS.
* an _InfrastructureComponent_ (i.e., the host on top of which we will deploy our SoftwareComponent, in our case a DockerEngine).

First, we start by creating the InternalComponent by clicking on 'SoftwareComponent > InternalComponent > Nodered' as depicted in the Figure below.

![alt text](docs/examples/1.nodered_localhost/images/create_component.png "Create Component")

At the current moment we just specify its 'name' and 'id' and click on the 'add' button to actually add the component to our deployment model.
A circle should appear in the editor! Please note that you can (i) zoom in/out but using the mouse wheel, (ii) you can move a component by drag and drop, and (iii) you can edit the properties of a component with a right click on it.

We can now create the InfrastructureComponent by clicking on 'InfrastructureComponent > Docker Engine'
At the current moment just specify it 'name' and 'id' and click on 'add'. A rectangle should appear!

We will now specify that our InternalComponent will be deployed on our InfrastructureComponent (i.e., Node-RED on Docker). To do so, we need to create a containement relationship between the two components by clicking on 'Add Link > Containment'.
Select the proper nodes and click on 'add'. The circle should now be contained by the rectangle as depicted in the Figure below.

![alt text](docs/examples/1.nodered_localhost/images/containment.png "Containment")

We are now going to configure our two components. 
For our InfrastructureComponent (aka., Docker Engine), we need to make sure that the 'IP' property of the component is set to '127.0.0.1'.
We can edit this property of the component by right-clicking on it and modifying the 'IP' field as depicted in the Figure below.

![alt text](docs/examples/1.nodered_localhost/images/port_container.png "Set port of the Docker engine")

For editing the InternalComponent (aka., Node-RED), we can use the GeneSIS JSON editor. You can move to the JSON editor view by clicking on the top right button named 'JSONEditor'.
We need to specify the Docker port binding (i.e., how the port of the service running in the Docker container will be accessible from outside).
We can edit the JSON as depicted in the figure below. Typically, Node-RED is exposed using port 1880.

![alt text](docs/examples/1.nodered_localhost/images/delete.png "Delete inputs in the JSON")
![alt text](docs/examples/1.nodered_localhost/images/port.png "Set port of the Docker binding")

Now it is time to deploy!

## Deploy

Click on 'Deploy > All'.

Once the deployment started, you can observe deployment logs in the console where you started GeneSIS. The deployment process will consist in (i) checking if the Docker engine is reachable (if so the rectangle will turn green), (ii) pulling the Node-RED docker image (whilst this operation is being performed the component will be yellow), and (iii) start the Docker container (At this point the circle should also be green as depicted in the Figure below).

![alt text](docs/examples/1.nodered_localhost/images/deployment.png "Successful deployment")

Finally, you can access to your Node-RED by right-clicking on the component in the graph view of the editor and by clicking on the “Go To” button.

## Deploy on a remote Host

So far, we deployed Node-RED on the machine running GeneSIS. But actually, we could have use the same process to deploy Node-RED on a remote host.
To do so, we only change the IP parameter of the InfrastructureComponent (aka., the host) to the IP address of the host where we want to deploy Node-RED.


## Deploy Node-RED and initialize it with a flow

When deploying an instance of the Node-RED runtime you can use the property _nr-flow_ to specify a Node-RED flow that should be deployed once the Node-RED runtime is up.
In this tutorial we can use the following flow.

        [{"id":"86457344.50e6b","type":"inject","z":"745a133b.dd6dec","name":"","topic":"","payload":"","payloadType":"none","repeat":"","crontab":"","once":false,"x":190,"y":242,"wires":[["9a142026.fa47f"]]},{"id":"9a142026.fa47f","type":"function","z":"745a133b.dd6dec","name":"add new layer","func":"msg.payload = {};\nmsg.payload.command = {};\n\nvar u = 'http://{s}.tile.openstreetmap.fr/hot/{z}/{x}/{y}.png';\nvar o = { maxZoom: 19, attribution: '&copy; OpenStreetMap'};\n\nmsg.payload.command.map = {name:\"OSMhot\", url:u, opt:o};\nmsg.payload.command.layer = \"OSMhot\";\n\nreturn msg;","outputs":1,"noerr":0,"x":420,"y":2420,"wires":[["c643e022.1816c"]]},{"id":"c643e022.1816c","type":"worldmap","z":"745a133b.dd6dec","name":"","x":750,"y":2460,"wires":[]},{"id":"2998e233.4ba64e","type":"function","z":"745a133b.dd6dec","name":"USGS Quake monitor csv re-parse","func":"msg.payload.lat = msg.payload.latitude;\nmsg.payload.lon = msg.payload.longitude;\nmsg.payload.layer = \"earthquake\";\nmsg.payload.name = msg.payload.id;\nmsg.payload.icon = \"globe\";\nmsg.payload.iconColor = \"orange\";\n\ndelete msg.payload.latitude;\ndelete msg.payload.longitude;\t\nreturn msg;","outputs":1,"noerr":0,"x":540,"y":2560,"wires":[["c643e022.1816c"]]},{"id":"e72c5732.9fa198","type":"function","z":"745a133b.dd6dec","name":"move and zoom","func":"msg.payload = { command:{layer:\"Esri Terrain\",lat:0,lon:0,zoom:3} };\nreturn msg;","outputs":1,"noerr":0,"x":420,"y":2460,"wires":[["c643e022.1816c"]]},{"id":"12317723.589249","type":"csv","z":"745a133b.dd6dec","name":"","sep":",","hdrin":true,"hdrout":"","multi":"one","ret":"\\n","temp":"","x":390,"y":2500,"wires":[["2998e233.4ba64e"]]},{"id":"10e5e5f0.8daeaa","type":"inject","z":"745a133b.dd6dec","name":"","topic":"","payload":"","payloadType":"none","repeat":"","crontab":"","once":false,"x":190,"y":2460,"wires":[["e72c5732.9fa198"]]},{"id":"b6917d83.d1bac","type":"http request","z":"745a133b.dd6dec","name":"","method":"GET","url":"http://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/2.5_day.csv","x":270,"y":2560,"wires":[["12317723.589249"]]},{"id":"3842171.4d487e8","type":"inject","z":"745a133b.dd6dec","name":"Quakes","topic":"","payload":"","payloadType":"none","repeat":"900","crontab":"","once":false,"x":200,"y":2500,"wires":[["b6917d83.d1bac"]]}]

This flow leverage a _worldmap_ node that needs to be installed in the Node-RED runtime before the flow can be executed. In GeneSIS, this can be specified via the _packages_ property. In the context of our deployment, this property should be assigned with the following value:

        ["node-red-contrib-web-worldmap"]

The Figure below depicts these two properties

![alt text](docs/examples/1.nodered_localhost/images/flow.png "Flow Node-RED")

After deployment, your should see the following flow in your Node-RED editor.

![alt text](docs/examples/1.nodered_localhost/images/editor.png "Editor Node-RED")

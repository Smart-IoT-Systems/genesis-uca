# Deploying Node-RED on a Docker engine

In this example we will simply deploy two linked Node-Red containers via Docker on the machine running GeneSIS. 

Here, we assume that (i) a Docker engine is up on the machine running GeneSIS and with the Docker Remote engine accessible, and (ii) GeneSIS is properly installed on the machine.

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

In this example, our deployment model will be composed of three components: 
* two _SoftwareComponent_ (i.e., the Node-RED container that will be deployed by GeneSIS) and more precisely an InternalComponent as its deployment life-cycle will be managed by GeneSIS.
* an _InfrastructureComponent_ (i.e., the host on top of which we will deploy our SoftwareComponent, in our case a DockerEngine).

First we start by creating the InfrastructureComponent by clicking on 'InfrastructureComponent > Docker Engine'
At the current moment just specify its 'name', 'id', 'IP', and the port of the Docker engine. We can add the component into the deployment model by  clicking on the 'add' button. A rectangle should appear!

Now we can create the first InternalComponent by clicking on 'SoftwareComponent > InternalComponent > Nodered'. 
We need to specify, the name and id of the component, the port it will be accessible from, and the Docker port binding (i.e., how the port of the service running in the Docker container will be accessible from outside).
Docker port binding take following form "port-for-outside:port-for-inside_container" Typically, Node-RED is exposed using port 1880.

We will now specify that our InternalComponent will be deployed on our InfrastructureComponent (i.e., Node-RED on Docker). To do so, we need to create a containement relationship between the two components by clicking on 'Add Link > Containment'.
Select the proper nodes and click on 'add'.

We can now create the second InternalComponent. For that we can follow the same process as below but we should make sure that we will expose our container on another port as 1880 is already used.
You can for instance proceed as depicted in the figure below.

![alt text](./images/port_binding.png "Specify port binding")

We can now specify that our InternalComponent will be deployed on our InfrastructureComponent (i.e., Node-RED on Docker). To do so, we need to create a containement relationship between the two components by clicking on 'Add Link > Containment'.
Select the proper nodes and click on 'add'.

Finally, we specify that our first InternalComponent will send data to the second one. For that we can use a _communication_ link between the two components.
To create this communication click on Add Link > Communication', select the two components, and click on the 'add' button.
The resulting deployment model should look as depicted in the Figure below.

![alt text](./images/model.png "Deployment model")

## Deploy

Click on 'Deploy > All'.

The deployment is completed when all components and arrows are green (see Figure below).

![alt text](./images/deployed.png "Successful deployment")

Finally, you can access to your Node-RED by right-clicking on the component in the graph view of the editor and by clicking on the “Go To” button.
You will see that components have been instanciated and configured in both Node-RED containers (see Figure Below). These are websocket components that will allow the communication between the two containers (please note that these components may take some time before being connected to each other). 

![alt text](./images/Node-red.png "Node-RED container")

That's all folks!



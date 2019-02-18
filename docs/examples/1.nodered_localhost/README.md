# Deploying Node-RED on a Docker engine

In this example we will simply deploy a Node-Red container on the machine running GeneSIS by using Docker.

Here, we assume that (i) a Docker engine is running on the machine running GeneSIS with the Docker Remote engine accessible, and (ii) GeneSIS is properly installed on the machine.

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

We need first to remember that the GeneSIS Modelling language is inspired by component-based approaches. Deployment models can be regarded as assemblies of components. 

In this example, our deployment model will be composed of two components: 
* a SoftwareComponent (i.e., the Node-RED container that will be deployed by GeneSIS) and more precisely an InternalComponent as its deployment life-cycle will be managed by GeneSIS.
* an InfrastructureComponent (i.e., the host on top of which we will deploy our SoftwareComponent, in our case a DockerEngine).

First, we start by creating the InternalComponent by clicking on 'SoftwareComponent > InternalComponent > Nodered' as depicted in the Figure below.

![alt text](docs/examples/1.nodered_localhost/images/create_component.png "Create Component")

At the current moment just specify it 'name' and 'id' and click on 'add'.
A circle should appear in the editor! Please note that you can (i) zoom in/out but using the mouse wheel, (ii) you can move a component by drag and drop, and (iii) you can edit the properties of a component with a right click on it.

We can now create the InfrastructureComponent by clicking on 'Click on 'InfrastructureComponent > InternalComponent > Docker Engine'
At the current moment just specify it 'name' and 'id' and click on 'add'. A rectangle should appear!

We will now specify that our InternalComponent will be deployed on our InfrastructureComponent (i.e., Node-RED on Docker). To do so, we need to create a containement relationship between the two components by clicking on 'Add Link > Containment'.
Select the proper nodes and click on 'add'. The circle should now be contained by the rectangle as depicted in the Figure below.

![alt text](docs/examples/1.nodered_localhost/images/containment.png "Containment")

We are now going to configure our two components. 
For our InfrastructureComponent (aka., Docker Engine), we need to make sure that the IP property of the component is set to 127.0.0.1.
We can edit this property of the component by right-clicking on it and modifying the property 'IP' as depicted in the Figure below.

![alt text](docs/examples/1.nodered_localhost/images/port_container.png "Set port of the Docker engine")

For editing the InternalComponent (aka., Node-RED), we can use the GeneSIS JSON editor. You can move to the JSON editor view by clicking on the top right button named 'JSONEditor'.
We need to specify the Docker port binding (i.e., how the port of the service running in the container will be accessible from outside).
We can edit the JSON as depicted in the figure below. Typically, node-red is exposed using port 1880.

![alt text](docs/examples/1.nodered_localhost/images/delete.png "Delete inputs in the JSON")
![alt text](docs/examples/1.nodered_localhost/images/port.png "Set port of the Docker binding")

Now it is time to deploy!

## Deploy

Click on 'Deploy > All'.

Once the deployment started, you can observe deployment logs in the console where you started GeneSIS. The deployment process will consist in (i) checking if the Docker engine is reachable (if so the rectangle will turn green), (ii) pulling the Node-RED docker image (whilst this operation is being performed the component will be yellow), and (iii) start the Docker container (At this point the circle should also be green as depicted in the Figure below).

![alt text](docs/examples/1.nodered_localhost/images/deployment.png "Successful deployment")

Finally, you can access to your Node-RED by right-clicking on the component in the graph view of the editor before clicking on the “Go To” button.

In the next tutorial we will observe how to deploy a ThingML component!


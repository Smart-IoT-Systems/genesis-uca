# Deploying CouchDB with Ansible.

In this example we will simply deploy CouchDB via Ansible on the machine running GeneSIS or any other machine accessible via SSH. 

Here, we assume that (i) Ansible is up on the machine running GeneSIS, and (ii) GeneSIS is properly installed on the machine.

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

In this example, our deployment model will be composed of two components: 
* a _SoftwareComponent_ (i.e., the CouchDB that will be deployed by GeneSIS) and more precisely an InternalComponent as its deployment life-cycle will be managed by GeneSIS.
* an _InfrastructureComponent_ (i.e., the host on top of which we will deploy our SoftwareComponent).

First, we start by creating the InfrastructureComponent by clicking on 'InfrastructureComponent > Device'.
In the creation form we specify its 'name' and 'id' and we fill the field 'IP' with the IP address of the host on top of which our program written in ThingML will be deployed. Once the form completed we can click on the 'add' button to actually add the component in the deployment model.
For details on how to manipulate and edit components with the GeneSIS editor, please refer to our [first tutorial](https://gitlab.com/enact/GeneSIS/tree/master/docs/examples/1.nodered_localhost).

We can now create the SoftwareComponent by clicking on 'SoftwareComponent > InternalComponent > Generic InternalComponent'.
We start by specifying, the 'name' and 'id' of the component, and the 'port' it will be accessible from (typically 5984 for CouchDB).

We can now define how to install, configure and start CouchDB by associating an _Ansible-Resource_ to our InternalComponent. In the GeneSIS Modelling language A _SoftwareComponent_ can be associated with _Resources_ (e.g., scripts, configuration files) adopted to manage its deployment life-cycle (i.e., download, configure, install, start, and stop).







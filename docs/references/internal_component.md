
# Reference definition for Internal Components

An Internal component is a subtype of Software Component. The life-cycle of Internal Components is fully managed by GeneSIS.

----

*  **name** *(string)*: the name of the component.
----
*  **_type** *(string)*: the type of the component, presented in the form of a path to describe the inheritance hierarchy.
----
*  **version** *(string)*: the version of the software component. Changing the version number is way to redeploy it.
----
*  **id** *(string)*: A unique identifier of the component. 
----
*  **docker_resource** *(object)*: A resource to manage the life-cycle of a software component when using Docker.
    - **name**: the name of the resource.
    - **image**: the Docker image to be manipulated.
    - **command**: A command to be executed when the container is started.
    - **port_binding**: The port binding of the container, in the form: `{"1880": "1880"}`
    - **devices**: To mount volumes, in the form: `{ "PathOnHost": '', "PathInContainer": '', "CgroupPermissions": "rwm" }`
----

*  **ssh_resource** *(object)*: A resource to manage the life-cycle of a software component using SSH commands.
    - **downloadCommand** *(String)*: First command executed to download the deployable artefact on the host.
    - **installCommand** *(String)*: Second command executed to install the deployable artefact. 
    - **configureCommand** *(String)*: Third command executed to configure the deployable artefact.
    - **startCommand** *(String)*: Fourth command executed to start the deployable artefact.
    - **stopCommand** *(String)*: Command executed when the software component is remove from the deployment model.
    - **credentials** *(object)*: Credentials to connect via SSH.
----

*  **provided_communication_port** *(object)*: Provide a communication facility.
    - **port_number** *(String)*: Port used for the communication.
    - **capabilities** *(object)*: Describes what is offered by the component on this port.
----

*  **required_communication_port** *(object)*: Require a communication facility.  
    - **port_number** *(String)*: Port used for the communication.
    - **isMandatory** *(Boolean)*: If this port is not connected to a provided_communication_port the software component will not function properly.
    - **capabilities** *(object)*: Describes what is required by the component on this port.
----
*  **required_execution_port** *(object)*: Provide an execution environment for other components to be hosted on.
    - **capabilities** *(object)*: Describes what is offered by the component on this port.
----
*  **provided_execution_port** *(object)*: Require an execution environment to be hosted on.
    - **capabilities** *(object)*: Describes what is required by the component on this port.

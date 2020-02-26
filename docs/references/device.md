
# Reference definition for Device Components

Device is a subtype of infrastructure Component. Thus, all the properties defined [here](../infrastructure_component.md) also apply.

----

* **physical_port** *(string)*: The physical port (e.g., serial: /dev/ttyACM0) used to interact with the device.

----

* **device_type** *(string)*: The type of device (e.g., arduino).

----

* **needDeployer** *(boolean)*: If a deployment agent is required to deploy software on this component.
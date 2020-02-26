# Reference definition for External Components

An Internal component is a subtype of Software Component. The life-cycle of External Components is **NOT** managed by GeneSIS.

----

* **ip** *(string)*: the IP address of the software component.

----

*  **provided_communication_port** *(object)*: Provide a communication facility.
    - **port_number** *(String)*: Port used for the communication.
    - **capabilities** *(object)*: Describes what is offered by the component on this port.

----

*  **required_communication_port** *(object)*: Require a communication facility.
    - **port_number** *(String)*: Port used for the communication.
    - **isMandatory** *(Boolean)*: If this port is not connected to a provided_communication_port the software component will not function properly.
    - **capabilities** *(object)*: Describes what is required by the component on this port.
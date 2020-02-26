# Reference definition for Node-RED Components.

An Node-RED component is a subtype of Internal Component. Thus, all the properties defined [here](../internal_component.md) also apply.

----


* **nr_flow** *(string)*: A node-red flow stringified. The flow will be instantiated when the Node-RED container is running.

----

* **path_flow** *(string)*: The path to a node-red flow. The flow will be instantiated when the Node-RED container is running.

----

* **packages** *(table of strings)*: The list of packages to be installed before instantiating a flow.
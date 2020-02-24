var mm = require('../metamodel/allinone.js');

/******************************/
/* Specific mqtt    component */
/******************************/
var mqtt = function (spec) {
    var that = mm.software_node(spec);
    that._type += "/mqtt";
    that.name = spec.name || "MQTTBroker";
    that.docker_resource= spec.docker_resource || {
        "name": "f3e3feba-056e-=26a7-9225-5b9edf5f1820",
        "image": "eclipse-mosquitto:1.6.8",
        "port_bindings": {
            "9001": "9001",
            "1883": "1883",
        },
        "devices": {
            "PathOnHost": "",
            "PathInContainer": "",
            "CgroupPermissions": "rwm"
        }
    };

    that.required_communication_port = spec.required_communication_port || [
        {
            "name": "bd3f34af-f691-4a46-b9cd-14bab0f9a8e0",
            "port_number": "1883",
            "isMandatory": false
        }
    ];

    that.provided_communication_port = spec.provided_communication_port || [
        {
            "name": "bd3provf-f691-4a46-b9cd-14bab0f9a8e0",
            "port_number": "1883",
            "isMandatory": false
        }
    ];

    return that;
};

module.exports = mqtt;

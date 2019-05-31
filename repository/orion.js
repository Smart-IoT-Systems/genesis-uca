var mm = require('../metamodel/allinone.js');

/******************************/
/* Specific Orion   component */
/******************************/
var orion = function (spec) {
    var that = mm.software_node(spec);
    that._type += "/orion";
    that.name = spec.name || "OrionBroker";
    that.docker_resource= spec.docker_resource || {
        "name": "f3e3feba-056e-46a7-9225-5b9edf5f1820",
        "image": "fiware/orion:2.2.0",
        "command": "-dbhost mongodb",
        "links": ["mongodb:mongodb"],
        "port_bindings": {
            "1026": "1026"
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
            "port_number": "80",
            "isMandatory": false
        },
        {
            "name": "requiresMongo",
            "port_number": "80",
            "isMandatory": true
        }
    ];


    return that;
};

module.exports = orion;

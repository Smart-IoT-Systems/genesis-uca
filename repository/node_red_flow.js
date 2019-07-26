var mm = require('../metamodel/allinone.js');

/******************************/
/* Specific Orion   component */
/******************************/
var node_red_flow = function (spec) {
    var that = mm.software_node(spec);
    that._type += "/node_red_flow";
    that.name = spec.name || "node-red-flow";
    
    that.nr_flow = spec.nr_flow || [];
    that.path_flow = spec.path_flow || "";
    that.packages = spec.packages || [];

    that.required_communication_port[0].port_number = '1880';

    return that;
};

module.exports = node_red_flow;

var mm = require('../metamodel/allinone.js');
var uuidv1 = require('uuid/v1');

/******************************/
/* Specific ThingML component */
/******************************/
var thingml = function (spec) {
    var that = mm.software_node(spec); //the inheritance
    that._type += "/thingml";
    that.name = spec.name || "MainCfg";
    that.nr_description = spec.nr_description || ""; 
    that.file= spec.file || "/Users/ferrynico/Documents/Code/GeneSIS-gitlab/docker-node-red-thingml/doc/examples/LightSensorApp/LightSensorApp.thingml";
    that.src = spec.src || "";
    that.libraries = spec.libraries || ["Adafruit GFX Library","Adafruit ST7735 Library"];
    that.target_language = spec.target_language || "java";
    that.config_name = spec.config_name || that.name;

    return that;
};

module.exports = thingml;
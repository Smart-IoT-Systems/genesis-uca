var mm = require('../metamodel/allinone.js');
var uuidv1 = require('uuid/v1');

/******************************/
/* Specific Arduino component */
/******************************/
var arduino = function (spec) {
    var that = mm.software_node(spec); //the inheritance
    that._type += "/arduino";
    that.name = spec.name || "Main";
    that.nr_description = spec.nr_description || ""; 
    that.sketch= spec.sketch || "/Users/ferrynico/Documents/Code/GeneSIS-gitlab/docker-node-red-thingml/doc/examples/LightSensorApp/LightSensorApp.thingml";
    that.libraries = spec.libraries || ["Adafruit GFX Library","Adafruit ST7735 and ST7789 Library"];

    return that;
};

module.exports = arduino;
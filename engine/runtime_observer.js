var bus = require('./event-bus.js');
var logger = require('./logger.js');

var runtime_observer = function (dm) {
    var that = {};

    that.d_m=dm;

    that.setRuntimeInfo = function (elem_name, key, value) {
        console.log(JSON.stringify(that.d_m));
        try{
            var elem = that.d_m.find_node_named(elem_name);
            if (elem._runtime === undefined) {
                elem._runtime = {};
            }
            elem._runtime[key] = value;
        }catch(err){
            logger.log('info', 'Error setting runtime info of '+ elem_name +'! ' + err+ JSON.stringify(elem));
        }
    }

    that.set_model = function(model){
        that.d_m = model;
    };

    that.start = function () {

        bus.on('container-error', function (comp_name) {
            that.setRuntimeInfo(comp_name, "Status", "error");
        });

        bus.on('host-config', function (comp_name) {
            console.log(">> config host");
            that.setRuntimeInfo(comp_name, "Status", "config");
        });

        bus.on('ansible-started', function (comp_name) {
            that.setRuntimeInfo(comp_name, "Status", "running");
        });

        bus.on('ssh-started', function (comp_name) {
            that.setRuntimeInfo(comp_name, "Status", "running");
        });

        bus.on('container-config', function (comp_name) {
            console.log(">> config container");
            that.setRuntimeInfo(comp_name, "Status", "config");
        });

        bus.on('container-started', function (container_id, comp_name) {
            console.log(">> container started");
            that.setRuntimeInfo(comp_name, "Status", "running");
            that.setRuntimeInfo(comp_name, "id", container_id);
        });

        bus.on('node-started', function (container_id, comp_name) {
            console.log(">> node started");
            that.setRuntimeInfo(comp_name, "Status", "running");
            that.setRuntimeInfo(comp_name, "id", container_id);
        });

    }

    return that;
};

module.exports = runtime_observer;
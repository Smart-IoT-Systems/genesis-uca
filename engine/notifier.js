var bus = require('./event-bus.js');

var notifier = (function (socket) {
    var that = {};
    that.socketObject = socket;

    that.start = function () {

        bus.on('container-error', function (comp_name) {
            //Send status info to the UI
            var s = {
                node: comp_name,
                status: 'error'
            };
            that.socketObject.send("#" + JSON.stringify(s));
        });

        bus.on('container-config', function (comp_name) {
            //basically, host is accessible
            var host_id = that.dep_model.find_node_named(comp_name).id_host;
            var h = {
                node: host_id,
                status: 'running'
            };
            that.socketObject.send("#" + JSON.stringify(h));

            //Send status info to the UI
            var s = {
                node: comp_name,
                status: 'config'
            };
            that.socketObject.send("#" + JSON.stringify(s));
        });

        bus.on('link-ok', function (link_name) {
            var s = {
                node: link_name,
                status: 'OK'
            };
            that.socketObject.send("#" + JSON.stringify(s));
        });

        bus.on('link-ko', function (link_name) {
            var s = {
                node: link_name,
                status: 'KO'
            };
            that.socketObject.send("#" + JSON.stringify(s));
        });

        bus.on('ansible-started', function (comp_name) {
            var s = {
                node: comp_name,
                status: 'running'
            };
            that.socketObject.send("#" + JSON.stringify(s));
        });

        bus.on('ssh-started', function (comp_name) {
            var s = {
                node: comp_name,
                status: 'running'
            };
            that.socketObject.send("#" + JSON.stringify(s));
        });

        bus.on('container-started', function (container_id, comp_name) {
            var s = {
                node: comp_name,
                status: 'running'
            };
            that.socketObject.send("#" + JSON.stringify(s));
        });

    }


    return that;
});

module.exports = notifier;
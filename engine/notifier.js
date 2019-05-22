var bus = require('./event-bus.js');

var notifier = (function (client, dm) {
    var that = {};
    that.MQTTClient = client;

    that.start = function () {
        
        that.MQTTClient.on('connect', function () {
            client.subscribe('/deployment_agent');
        });

        that.MQTTClient.on('message', function (topic, message) {
            if (topic === '/deployment_agent') {
                var json = JSON.parse(message);
                console.log(message);
            }
        });

        bus.on('remove-all', function(){
            that.MQTTClient.publish("/Notifications", JSON.stringify("Remove all completed!"));
        });

        bus.on('container-error', function (comp_name) {
            //Send status info to the UI
            var s = {
                node: comp_name,
                status: 'error'
            };
            that.MQTTClient.publish("/Status", JSON.stringify(s));
        });

        bus.on('host-config', function (comp_name) {
            //basically, host is accessible
            var h = {
                node: comp_name,
                status: 'running'
            };
            that.MQTTClient.publish("/Status", JSON.stringify(h));

        });

        bus.on('link-ok', function (link_name) {
            var s = {
                node: link_name,
                status: 'OK'
            };
            that.MQTTClient.publish("/Status", JSON.stringify(s));
        });

        bus.on('link-ko', function (link_name) {
            var s = {
                node: link_name,
                status: 'KO'
            };
            that.MQTTClient.publish("/Status", JSON.stringify(s));
        });

        bus.on('ansible-started', function (comp_name) {
            var s = {
                node: comp_name,
                status: 'running'
            };
            that.MQTTClient.publish("/Status", JSON.stringify(s));
        });

        bus.on('ssh-started', function (comp_name) {
            var s = {
                node: comp_name,
                status: 'running'
            };
            that.MQTTClient.publish("/Status", JSON.stringify(s));
        });

        bus.on('container-config', function (comp_name) {
            //Send status info to the UI
            var s = {
                node: comp_name,
                status: 'config'
            };
            that.MQTTClient.publish("/Status", JSON.stringify(s));
        });

        bus.on('container-started', function (container_id, comp_name) {
            var s = {
                node: comp_name,
                status: 'running'
            };
            that.MQTTClient.publish("/Status", JSON.stringify(s));
        });

        bus.on('node-started', function (container_id, comp_name) {
            var s = {
                node: comp_name,
                status: 'running'
            };
            that.MQTTClient.publish("/Status", JSON.stringify(s));
        });

    }


    return that;
});

module.exports = notifier;
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
                var content = JSON.parse(message);
                if (content.data.status === "success") {
                    bus.emit('d_agent_success', content.target_name);
                } else {
                    bus.emit('d_agent_error', content.target_name);
                }
            }
        });


        bus.on('deploy-started', function () {
            that.MQTTClient.publish("/Notifications", JSON.stringify("A deployment is started!"));
        });

        bus.on('remove-all', function () {
            that.MQTTClient.publish("/Notifications", JSON.stringify("Remove all completed!"));
        });

        bus.on('deployment-completed', function () {
            that.MQTTClient.publish("/Notifications", JSON.stringify("Deployment completed!"));
        });

        bus.on('container-error', function (comp_name) {
            //Send status info to the UI
            var s = {
                node: comp_name,
                status: 'error'
            };
            that.MQTTClient.publish("/Status", JSON.stringify(s));
        });

        bus.on('node-error', function (container_id, comp_name) {
            //Send status info to the UI
            var s = {
                node: comp_name,
                status: 'error'
            };
            that.MQTTClient.publish("/Status", JSON.stringify(s));
            bus.emit('node-error2', container_id, comp_name);
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
            bus.emit('node-started2', container_id, comp_name);
        });

        bus.on('runtime-info', function (comp_name, status) {
            var s = {
                node: comp_name,
                status: status
            };
            that.MQTTClient.publish("/Status", JSON.stringify(s));
        });

        bus.on('removed', function (comp_name) {
            var s = {
                node: comp_name,
                status: 'config'
            };
            that.MQTTClient.publish("/Status", JSON.stringify(s));
        });

        bus.on('tas', function (body) {
            var s = {
                result: body,
                status: 'test_result_ready'
            };
            that.MQTTClient.publish("/Status", JSON.stringify(s));
        });
    }


    return that;
});

module.exports = notifier;
var logger = require('./logger.js');
var aedes = require('aedes')();

var MQTTBroker = (function () {
    var that = {};
    that.server = require('net').createServer(aedes.handle);
    that.port = 1883;

    that.start = function () {

        that.server.listen(that.port, function () {
            logger.log("info",'MQTT server listening on port ' + that.port)
        });

        aedes.on('clientError', function (client, err) {
            logger.log("info",'client error', client.id, err.message, err.stack)
        });

        aedes.on('connectionError', function (client, err) {
            logger.log("info",'client error', client, err.message, err.stack)
        });

        aedes.on('publish', function (packet, client) {
            if (client) {
                logger.log("info",'message from client', client.id)
            }
        });

        aedes.on('subscribe', function (subscriptions, client) {
            if (client) {
                logger.log("info",'subscribe from client', subscriptions, client.id)
            }
        });

        aedes.on('client', function (client) {
            logger.log("info",'new client', client.id)
        });

    }

    return that;
}());

module.exports = MQTTBroker;
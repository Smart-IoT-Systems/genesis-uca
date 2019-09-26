var logger = require('./logger.js');
var aedes = require('aedes')();
var ws = require('websocket-stream');

var MQTTBroker = (function () {
    var that = {};
    that.server = require('http').createServer();
    that.port = 9001;

    that.start = function () {

        ws.createServer({
            server: that.server
        }, aedes.handle);

        that.server.listen(that.port, "0.0.0.0", function () {
            logger.log("info", 'MQTT websocket server listening on :'+ that.port);
        });

        aedes.on('clientError', function (client, err) {
            if(err.message !== 'keep alive timeout'){
                logger.log("info", 'client error ' + client.id + " " + err.message + " " + err.stack);
            }
        });

        aedes.on('connectionError', function (client, err) {
            if(err.message !== 'connect did not arrive in time'){
                logger.log("info", 'connection error ' + client.id + " " + err.message + " " + err.stack);
            }
        });

        aedes.on('publish', function (packet, client) {

        });

        aedes.on('subscribe', function (subscriptions, client) {
            if (client) {
                logger.log("info", 'subscribe from client ' + subscriptions + " from " + client.id);
            }
        });

        aedes.on('client', function (client) {
            logger.log("info", 'New MQTT client ' + client.id);
        });

    }

    return that;
}());

module.exports = MQTTBroker;
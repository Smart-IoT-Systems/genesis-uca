var mosca = require('mosca');
var logger = require('./logger.js');

var MQTTBroker = (function () {
    var that = {};

    that.start = function () {
        var settings = {
            port: 1883
        };

        var server = new mosca.Server(settings);

        server.on('clientConnected', function (client) {
            logger.log("info", 'Client connected to the GeneSIS broker' + client.id);
        });

        // fired when a message is received
        server.on('published', function (packet, client) {
            logger.log("info", 'Published: '+ packet.payload);
        });

        server.on('ready', setup);

        // fired when the mqtt server is ready
        function setup() {
            logger.log("info", 'Mosca server is up and running');
        }
    }

    return that;
}());

module.exports = MQTTBroker;
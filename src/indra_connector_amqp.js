var fs = require('fs');
var ampq = require('amqplib');
var fetch = require("node-fetch");
var conf = require("./config_indra.json");


var indra_connector_amqp = function (deployment_model) {

    var that = {};
    that.KEY = fs.readFileSync(conf.KEY);
    that.CERT = fs.readFileSync(conf.cert);
    that.CAfile = [fs.readFileSync(conf.CAfile)];
    that.login = conf.login;
    that.password = conf.passwd;
    that.deployment_model = deployment_model;
    that.ip=conf.ip;

    that.open_connection = function () {
        var opts = {
            port: 5671,
            cert: that.CERT,
            key: that.KEY,
            ca: that.CAfile,
            rejectUnauthorized: false
        };

        var q = 'SINTEF_Q';

        var open = ampq.connect('amqps://'+that.login+':'+that.password+'@'+that.ip+':5671/MQTT_IO', opts);

        // Consumer
        open.then(function (conn) {
            console.log("Connected!");
            that.trigger_deployment();
            return conn.createChannel();
        }).then(function (ch) {
            return ch.assertQueue(q).then(function (ok) {
                return ch.consume(q, function (msg) {
                    if (msg !== null) {
                        console.log(msg.content.toString());
                        ch.ack(msg);
                        //trigger_deployment();
                    }
                });
            });
        }).catch(console.warn);
    };

    that.trigger_deployment = function () {
        fetch('http://127.0.0.1:8000/genesis/deploy', {
                method: 'POST',
                headers: {
                    Accept: 'application/json',
                    'Content-Type': 'application/json'
                },
                body: that.deployment_model
            }).then(response => response.json())
            .then(response => {
                if (response.started) {
    
                }
            }).catch(function(err){
                console.log("Could not connect to GeneSIS API \n>> "+JSON.stringify(err));
            });
    };

    return that;
};
module.exports = indra_connector_amqp;

var dm = fs.readFileSync('../docs/examples/INDRA-Fiware.json', 'utf-8');
var c = indra_connector_amqp(dm);

 c.open_connection();





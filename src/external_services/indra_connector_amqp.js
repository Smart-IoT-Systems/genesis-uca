var fs = require('fs');
var ampq = require('amqplib');
var fetch = require("node-fetch");
var conf = require("./config_indra.json");
var example_data1 = require("./packet.json");
var example_data2 = require("./packet2.json");


var indra_connector_amqp = function (deployment_model) {

    var that = {};
    that.KEY = fs.readFileSync(conf.KEY);
    that.CERT = fs.readFileSync(conf.cert);
    that.CAfile = [fs.readFileSync(conf.CAfile)];
    that.login = conf.login;
    that.password = conf.passwd;
    that.deployment_model = deployment_model;
    that.ip = conf.ip;
    that.triggered = false;


    that.connect = function () {
        var opts = {
            port: 5671,
            cert: that.CERT,
            key: that.KEY,
            ca: that.CAfile,
            rejectUnauthorized: false
        };

        that.open = ampq.connect('amqps://' + that.login + ':' + that.password + '@' + that.ip + ':5671/MQTT_IO', opts);
    }

    that.listen = function () {
        var q = 'SINTEF_Q';

        // Consumer
        that.open.then(function (conn) {
            console.log("Connected!");
            return conn.createChannel();
        }).then(function (ch) {
            return ch.assertQueue(q).then(function (ok) {
                return ch.consume(q, function (msg) {
                    if (msg !== null) {
                        console.log(msg.content.toString());
                        var packet = msg.content.toString();
                        var packet_object = JSON.parse(msg.content);
                        ch.ack(msg);
                        //Dummy behavior for the demo
                        if (packet_object.Nodes[0]["Sensors-Actuators"][0].Resources["5700"] === 1) {
                            //Change version in deployment model
                            if (!that.triggered) {
                                that.triggered = true;
                                var query = {
                                    "name": "GTW_app",
                                    "attribute": "version",
                                    "value": "0.0.2"
                                };
                                that.update_model(query);
                            }

                        }
                    }
                });
            });
        }).catch(console.warn);

    };

    that.publish = function (data) {
        var q = 'SINTEF_Q';

        that.open.then(function (conn) {
            return conn.createChannel();
        }).then(function (ch) {
            return ch.assertQueue(q).then(function (ok) {
                return ch.sendToQueue(q, Buffer.from(JSON.stringify(data)));
            });
        }).catch(console.warn);
    };

    that.update_model = function (query) {
        console.log("Updating model");
        fetch('http://127.0.0.1:8000/genesis/component', {
            method: 'POST',
            headers: {
                Accept: 'application/json',
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(query)
        }).then(response => {
            if (response.statusText === "OK") {
                that.trigger_deployment();
            }
        }).catch(function (err) {
            console.log("Could not connect to GeneSIS API \n>> " + JSON.stringify(err));
        });
    };

    that.trigger_deployment = function () {
        fetch('http://127.0.0.1:8000/genesis/deploy_model')
            .then(response => {
                if (response.statusText === "OK") {
                    console.log("Deployment triggered");
                }
            }).catch(function (err) {
                console.log("Could not connect to GeneSIS API \n>> " + JSON.stringify(err));
            });
    };

    return that;
};
module.exports = indra_connector_amqp;


//Behavior for the demo.
var dm = fs.readFileSync('../docs/examples/INDRA-Fiware.json', 'utf-8');
var c = indra_connector_amqp(dm);

c.connect();
c.listen();
/*setTimeout(function(){
    c.publish(example_data1);
    setTimeout(function(){
        c.publish(example_data1);
        setTimeout(function(){
            c.publish(example_data2);
        }, 9000);
    }, 5000);
}, 3000);*/
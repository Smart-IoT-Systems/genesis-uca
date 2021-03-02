/*
 * The deployment agent is reponsible for deployment software on devices that cannot be directly 
 * accessed from the GeneSIS runtime. An agent is an instance of Node-red with a set of specific
 * 'GeneSIS' nodes
 */

var emitter = require('events').EventEmitter;
var dc = require('./connectors/docker-connector.js');
var uuidv4 = require('uuid/v4');
var http = require('http');
var logger = require('./logger.js');
var bus = require('./event-bus.js');
var fs = require('fs');

var deployment_agent = function (host, host_target, deployment_target) {
    var that = {};
    that.emitter = new emitter();
    that.host = host;
    that.host_target = host_target;
    that.deployment_target = deployment_target;
    that.flow = '';


    that.compute_ip_address = function () {
        return new Promise(function (resolve, reject) {
            if (process.env.IP_GENESIS !== undefined) {
                resolve(process.env.IP_GENESIS);
            } else {
                const internalIp = require('internal-ip');
                var result = internalIp.v4.sync();
                resolve(result);
            }
        });
    };


    //We need to identify what should be in the deployment agent
    that.prepare = async function () {

        that.ip = await that.compute_ip_address();

        that.flow = '[{"id":"dac41de7.a03033","type":"tab","label":"Deployment Flow"}';
        if (host_target._type === "/infra/device") {
            var id_deployer_node_in_agent = uuidv4();
            if ((host_target.device_type.indexOf("ardui") !== -1) || (host_target.device_type.indexOf("uno") !== -1)
                || (host_target.device_type.indexOf("mega") !== -1) || (host_target.device_type.indexOf("leonardo") !== -1)) {
                var lib_strigified = JSON.stringify(that.deployment_target.libraries);
                var arduitype = host_target.device_type || "uno";
                var arduicpu = host_target.cpu || "";
                console.log("ssssss>" + JSON.stringify(host_target));
                if (deployment_target._type === "/internal/arduino") {
                    var ino_content = fs.readFileSync(deployment_target.sketch, "utf8");
                    var ino_stringified = JSON.stringify(ino_content);
                    that.flow += ',{"id":"' + uuidv4() + '","type":"inject","z":"dac41de7.a03033","name":"","topic":"","payload":"","payloadType":"str","repeat":"","crontab":"","once":true,"onceDelay":20,"x":230,"y":140,"wires":[["e23ea497.5b6678"]]}, {"id":"e23ea497.5b6678","type":"function","z":"dac41de7.a03033","name":"","func":' + JSON.stringify('msg.payload=' + ino_stringified + ';\nreturn msg;') + ',"outputs":1,"noerr":0,"x":410,"y":260,"wires":[["812a8df2.aa502"]]},{"id":"812a8df2.aa502","type":"file","z":"dac41de7.a03033","name":"","filename":"/data/tmp/tmp.ino","appendNewline":true,"createDir":true,"overwriteFile":"true","x":420,"y":140,"wires":[["e22ea497.5b6678"]]},{"id":"e22ea497.5b6678","type":"function","z":"dac41de7.a03033","name":"","func":' + JSON.stringify('msg.payload={};\nmsg.payload.name= "tmp";\nmsg.payload.output="/data";\nreturn msg;') + ',"outputs":1,"noerr":0,"x":410,"y":260,"wires":[["' + id_deployer_node_in_agent + '"]]}';
                }
                that.flow += ',{"id": "' + id_deployer_node_in_agent + '","type": "arduino","z": "dac41de7.a03033","name": "' + that.host_target.name + '","serial": "7d118b53.12e99c","ardtype": "' + arduitype + '","cpu": "' + arduicpu + '","libraries": ' + JSON.stringify(lib_strigified) + ',"x": 590,"y": 260, "wires": [["39ea4abb.22c316"]]},';
                that.flow += '{"id":"39ea4abb.22c316","type":"function","z":"dac41de7.a03033","name":"add_target_name","func":' + JSON.stringify('var newmsg={ payload: {}};\nnewmsg.payload.target_name=' + JSON.stringify(that.deployment_target.name) + ';\nnewmsg.payload.data=msg.payload;\nreturn newmsg;') + ',"outputs":1,"noerr":0,"x":310,"y":940,"wires":[["4355eb1b.25b744"]]},'
                that.flow += '{"id": "4355eb1b.25b744","type": "mqtt out","z": "dac41de7.a03033","name": "toGeneSIS","topic": "/deployment_agent","qos": "0","retain": "true","broker": "758af4ba.66f854","x": 690,"y": 320,"wires": []},{"id": "7d118b53.12e99c","type": "serial-port","z": "","serialport": "' + that.host_target.physical_port + '","serialbaud": "9600","databits": "8","parity": "none","stopbits": "1","newline": "\\n","bin": "false","out": "char","addchar": false},{"id":"758af4ba.66f854","type":"mqtt-broker","z":"","name":"GeneSIS","broker":"ws://' + that.ip + ':9001","port":"9001","clientid":"","usetls":false,"compatmode":true,"keepalive":"6000","cleansession":true,"birthTopic":"","birthQos":"0","birthRetain":"false","birthPayload":"","closeTopic":"","closeQos":"0","closeRetain":"false","closePayload":"","willTopic":"","willQos":"0","willRetain":"false","willPayload":""}';
            }
            if (deployment_target._type === "/internal/thingml") {
                var language = "nodejs";
                if ((host_target.device_type.indexOf("ardui") !== -1) || (host_target.device_type.indexOf("uno") !== -1) || (host_target.device_type.indexOf("mega") !== -1)) {
                    language = "arduino";
                }
                var thingml_stringified = deployment_target.code;
                if (deployment_target.file !== '') {
                    thingml_stringified = fs.readFileSync(deployment_target.file, "utf8");
                }
                that.flow += ',{"id": "' + uuidv4() + '","type": "thingml-compiler","z": "dac41de7.a03033","name": "' + that.deployment_target.name + '","active": true,"target": "' + language + '","triggerModified": "false","code": ' + JSON.stringify(thingml_stringified) + ',"source": "","sourcetype": "","sourcepath": "","x": 220,"y": 260, "wires": [["' + id_deployer_node_in_agent + '"]]}';
            }
        }
        that.flow += ']';
        return that.flow;
    };

    that.sleep = function (ms) {
        return new Promise(resolve => setTimeout(resolve, ms));
    };

    that.untilConnect = async function (tgt_host, tgt_port) {
        var can = false;
        while (!can) {
            logger.log("info", "Trying to connect to Node-RED " + tgt_host + ":" + tgt_port);
            await that.sleep(6000);
            can = await that.tryConnect(tgt_host, tgt_port);
        }
        await that.sleep(3000);
        return can;
    };

    that.tryConnect = function (tgt_host, tgt_port) {
        return new Promise(function (resolve, reject) {
            var opt = {
                host: tgt_host,
                path: '/flows',
                port: tgt_port
            };
            http.get(opt, function (resp) {
                resp.on('data', function (chunk) {
                    logger.log("info", "Connected");
                    resolve(true);
                });
            }).on("error", function (e) {
                resolve(false);
            });
        });
    };

    that.setFlow = function (tgt_host, tgt_port, data) {
        var options = {
            host: tgt_host,
            path: '/flows', //The Flows API of nodered, which set the active flow configuration
            port: tgt_port,
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Node-RED-Deployment-Type': 'flows' //only flows that contain modified nodes are stopped before the new configuration is applied.
            }
        };

        var req = http.request(options, function (response) {
            var str = ''
            response.on('data', function (chunk) {
                str += chunk;
            });

            response.on('end', function () {
                bus.emit('deployment-agent-started', tgt_host)
                logger.log("info", "Deployment agent flow deployed " + str);
            });
        });

        req.on('error', function (err) {
            logger.log("error", "Connection to " + tgt_host + " not yet open");
            setTimeout(function () {
                that.setFlow(tgt_host, tgt_port, data); //we try to reconnect if the connection as failed
            }, 5000);
        });


        //This is the data we are posting, it needs to be a string or a buffer 
        console.log(data);
        req.write(data);
        req.end();
    };

    that.install = async function (port_mapping) {
        var connector = dc();
        var mapping = {};
        mapping[port_mapping] = "1880";

        //We start Node-red
        var id = await connector.buildAndDeploy(that.host.ip, that.host.port, mapping, {
            "PathOnHost": that.host_target.physical_port,
            "PathInContainer": that.host_target.physical_port,
            "CgroupPermissions": "rwm"
        }, "", "nicolasferry/multiarch-node-red-thingml:latest", "", "", that.deployment_target.name, that.host.name);

        var readyToGo = await that.untilConnect(that.host.ip, port_mapping);
        if (readyToGo) {
            that.setFlow(that.host.ip, port_mapping, that.flow);
        }
        return id;
    };

    return that;
};

module.exports = deployment_agent;
var bus = require('../event-bus.js');
var logger = require('../logger.js');
var http = require('http');


var nodered_connector = function () {
    var that = {};

    that.installNodeType = function (tgt_host, tgt_port, data) {
        return new Promise(function (resolve, reject) {
            var options = {
                host: tgt_host,
                path: '/nodes',
                port: tgt_port,
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                }
            };

            var req = http.request(options, function (response) {
                var str = ''
                response.on('data', function (chunk) {
                    str += chunk;
                    logger.log("info", "data " + chunk);
                });

                response.on('end', function () {
                    logger.log("info", "Install Request completed ");
                    bus.emit('node installed', str);
                    resolve(str);
                });

            });

            req.on('error', function (err) {
                reject(err);
            });

            req.write(data);
            req.end();
        });
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

    that.installAllNodeTypes = async function (tgt_host, tgt_port, tab) {
        var allResult = "";
        var readyToGo = await that.untilConnect(tgt_host, tgt_port);
        if (readyToGo) {
            for (var t in tab) {
                var data = '{"module": "' + tab[t] + '"}'
                logger.log("info", "Installing on Node-red module: " + data);
                allResult += await that.installNodeType(tgt_host, tgt_port, data).catch((err) => {
                    //logger.log("info", data + " " + err);
                });
            }
            return allResult;
        }
    };

    that.getCurrentFlow = async function (tgt_host, tgt_port) {
        const readyToGo = await that.untilConnect(tgt_host, tgt_port);
        if (readyToGo) {
            const _flow = await that.getFlow(tgt_host, tgt_port);
            return _flow;
        }
    };

    that.getFlow = function (tgt_host, tgt_port) {
        return new Promise(function (resolve, reject) {
            var opt = {
                host: tgt_host,
                path: '/flows', //The Flows API of nodered, which set the active flow configuration
                port: tgt_port
            };
            http.get(opt, function (resp) {
                var _string = "";
                resp.on('data', function (chunk) {
                    _string += chunk;
                });

                resp.on('end', function () {
                    var d_flow = [];
                    if (Array.isArray(JSON.parse(_string))) {
                        d_flow = JSON.parse(_string);
                    }
                    resolve(d_flow);
                });
            }).on("error", function (e) {
                logger.log("error", "Cannot get current Node-red flow: " + e.message);
                reject(e);
            });
        });
    }

    //TO be migrated in a node-red connector
    that.setFlow = function (tgt_host, tgt_port, data, tgt_tab, src_tab, dm) {
        return new Promise(async function (resolve, reject) {
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
                var str = '';
                response.on('data', function (chunk) {
                    str += chunk;
                });

                response.on('end', function () {
                    logger.log("info", "Request to upload Node-Red flow completed on: " + tgt_host + ":" + tgt_port);
                    logger.log("info", "Response: "+Buffer.byteLength(data, 'utf8'));
                    logger.log("info", "Response: "+Buffer.byteLength(data));
                    logger.log("info", "Response: "+data.length);
                    if(JSON.parse(data).length > 0){
                        for (var w in tgt_tab) { //if success, send feedback
                            bus.emit('link-ok', tgt_tab[w].name);
                        }
                        for (var p in src_tab) { //if success, send feedback
                            var a = dm.get_comp_name_from_port_id(src_tab[p].target);
                            if (dm.find_node_named(a).nr_description !== undefined) {
                                bus.emit('link-ok', src_tab[p].name);
                            }
                        }
                    }
                    logger.log("info", "Response: "+str);
                    resolve();
                });

            });

            req.on('error', function (err) {
                logger.log("info", "Connection to " + tgt_host + ":" + tgt_port + " not yet open");
                setTimeout(function () {
                    for (var w in tgt_tab) {
                        bus.emit('link-ko', tgt_tab[w].name);
                    }
                    that.setFlow(tgt_host, tgt_port, data, tgt_tab); //we try to reconnect if the connection as failed
                }, 5000);
            });


            //This is the data we are posting, it needs to be a string or a buffer
            //req.write(data);
            req.end(data);
        });
    }

    return that;
};

module.exports = nodered_connector;
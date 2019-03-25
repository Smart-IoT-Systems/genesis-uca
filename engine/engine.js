var webSocketServer = require('ws').Server;
var http = require('http');
var mm = require('../metamodel/allinone.js');
var dc = require('./connectors/docker-connector.js');
var sshc = require('./connectors/ssh-connector.js');
var bus = require('./event-bus.js');
var uuidv4 = require('uuid/v4');
var comparison_engine = require('./model-comparison.js');
var class_loader = require('./class-loader.js');
var agent = require('./deployment-agent.js');
var logger = require('./logger.js');
var ac = require('./connectors/ansible-connector.js');
var thingmlcli = require('./thingml-compiler.js');
var mvn_builder = require('maven');
var notifier = require('./notifier');
var nodered_connector = require('./connectors/nodered_connector.js');
var fs = require('fs');

var engine = (function () {
    var that = {};
    //that.dep_model = 'undefined';
    that.dep_model = mm.deployment_model({});
    that.diff = {};

    that.webSocketServerObject = new webSocketServer({
        port: 9060
    });

    that.socketObject = {};


    that.remove_containers = async function (diff) {
        var removed = diff.list_of_removed_components;
        var connector = dc();
        if (removed.length === 0) {
            return;
        }
        for (var i in removed) {
            var host_id = removed[i].id_host;
            var host = that.dep_model.find_node_named(host_id); //This cannot be done!
            if (host === undefined) {
                //Need to find the host in the old model
                var rem_hosts = diff.list_removed_hosts;
                for (var z in rem_hosts) {
                    if (rem_hosts[z].name === host_id) {
                        if (rem_hosts[z]._type === "docker_host") {
                            await connector.stopAndRemove(removed[i].container_id, rem_hosts[z].ip, rem_hosts[z].port);
                        }
                    }
                }
            } else {
                if (host._type === "docker_host") {
                    await connector.stopAndRemove(removed[i].container_id, host.ip, host.port);
                }
            }
        }
        bus.emit('remove-all');
    };

    that.run = async function (diff) { //TODO: factorize
        //var comp = dm.get_all_hosted();
        var comp = diff.list_of_added_hosted_components;
        var nb = 0;
        var tmp = 0;
        //var links_deployer_tab = dm.get_all_deployer_links();
        var links_deployer_tab = diff.list_of_added_links_deployer;

        //Deployment agent
        for (var l in links_deployer_tab) {
            var tgt_agent = that.dep_model.find_node_named(links_deployer_tab[l].target);
            var host_agent = that.dep_model.find_node_named(links_deployer_tab[l].src);
            var tgt_agent_host_id = tgt_agent.id_host;
            var tgt_agent_host = that.dep_model.find_node_named(tgt_agent_host_id);
            var src__agent_host_id = host_agent.id_host;
            var src_agent_host = that.dep_model.find_node_named(src__agent_host_id);

            var d_agent = agent(src_agent_host, tgt_agent_host, tgt_agent);
            d_agent.prepare();
            d_agent.install();
        }

        for (var i in comp) {
            (function (comp, i) {
                //if not to be deployed by a deployment agent
                if (!that.dep_model.need_deployment_agent(comp[i])) {

                    var host_id = comp[i].id_host;
                    var host = that.dep_model.find_node_named(host_id);
                    if (host !== undefined) {
                        if (comp[i]._type === "thingml") {
                            //we should generate the plantuml
                            var tcli = thingmlcli(comp[i]);
                            tcli.build("./generated_uml_" + comp[i].name, "uml").catch(function (err) {
                                logger.log("error", err);
                            });

                            //Then we generate for the target
                            tcli.build("./generated_" + comp[i].name, comp[i].target_language).then(function (elem) {
                                //if java we need to build and deploy
                                if (comp[i].target_language === 'java') {

                                    logger.log("info", process.cwd() + "/generated_" + comp[i].name);
                                    var mb = mvn_builder.create({
                                        cwd: process.cwd() + "/generated_" + comp[i].name
                                    });
                                    mb.execute(['clean', 'install'], {
                                        'skipTests': true
                                    }).then(function () {
                                        //TODO: make it more generic
                                        //as a start we connect and deploy via SSH
                                        var sc = sshc(host.ip, host.port, comp[i].ssh_resource.credentials.username, comp[i].ssh_resource.credentials.password, comp[i].ssh_resource.credentials.sshkey);
                                        sc.upload_file("./generated_" + comp[i].name + '/target/' + comp[i].name + '-1.0.0-jar-with-dependencies.jar', '/home/' + comp[i].ssh_resource.credentials.username + '/' + comp[i].name + '-1.0.0-jar-with-dependencies.jar').then(function (file_path_tgt) {
                                            sc.execute_command(comp[i].ssh_resource.startCommand);
                                            bus.emit('ssh-started', host.name);
                                            bus.emit('ssh-started', comp[i].name);
                                        }).catch(function (err) {
                                            logger.log("error", err);
                                        });
                                    }).catch(function (err) {
                                        logger.log("error", "mvn clean install failed: " + err);
                                    });
                                }
                                if (comp[i].target_language === 'nodejs') {
                                    logger.log("info", process.cwd() + "/generated_" + comp[i].name);
                                    var sc = sshc(host.ip, host.port, comp[i].ssh_resource.credentials.username, comp[i].ssh_resource.credentials.password, comp[i].ssh_resource.credentials.sshkey);
                                    sc.upload_directory("./generated_" + comp[i].name, '/home/' + comp[i].ssh_resource.credentials.username + '/generated_' + comp[i].name).then(function (file_path_tgt) {
                                        sc.execute_command(comp[i].ssh_resource.startCommand);
                                        bus.emit('ssh-started', host.name);
                                        bus.emit('ssh-started', comp[i].name);
                                    }).catch(function (err) {
                                        logger.log("error", err);
                                    });;
                                }
                            }).catch(function (err) {
                                logger.log("error", err);
                            });
                        } else {
                            if (host._type === "docker_host") {
                                var connector = dc();
                                nb++;
                                if (comp[i]._type === "node_red") {
                                    //then we deploy node red
                                    //TODO: what if port_bindings is empty?
                                    connector.buildAndDeploy(host.ip, host.port, comp[i].docker_resource.port_bindings, comp[i].docker_resource.devices, "", "nicolasferry/multiarch-node-red-thingml:latest", comp[i].docker_resource.mounts, comp[i].name, host.name).then(function (id) {
                                        if ((comp[i].nr_flow !== undefined && comp[i].nr_flow !== "") ||
                                            (comp[i].path_flow !== "" && comp[i].path_flow !== undefined)) { //if there is a flow to load with the nodered node
                                            let noderedconnector = nodered_connector();
                                            var _data = "";
                                            if (comp[i].path_flow !== "" && comp[i].path_flow !== undefined) {
                                                _data = fs.readFileSync(comp[i].path_flow);
                                            } else {
                                                _data = JSON.stringify(comp[i].nr_flow);
                                            }
                                            noderedconnector.installAllNodeTypes(host.ip, comp[i].port, comp[i].packages).then(function () {
                                                noderedconnector.setFlow(host.ip, comp[i].port, _data, [], [], that.dep_model);
                                                bus.emit('node-started', id, comp[i].name);
                                            });
                                        }
                                    });
                                } else {
                                    connector.buildAndDeploy(host.ip, host.port, comp[i].docker_resource.port_bindings, comp[i].docker_resource.devices, comp[i].docker_resource.command, comp[i].docker_resource.image, comp[i].docker_resource.mounts, comp[i].name, host.name).then(function () {
                                        bus.emit('node-started', id, comp[i].name);
                                    });
                                }
                            }
                            if (comp[i].ansible_resource.playbook_path !== "" && comp[i].ansible_resource.playbook_path !== undefined) {
                                var connector = ac(host, comp[i]);
                                connector.executePlaybook();
                            }
                            if (comp[i].ssh_resource.credentials.sshkey !== "") {
                                var sc = sshc(host.ip, host.port, comp[i].ssh_resource.credentials.username, comp[i].ssh_resource.credentials.sshkey, comp[i].ssh_resource.credentials.sshkey);
                                //just for fun 0o' let's try the most crappy code ever!
                                sc.execute_command(comp[i].ssh_resource.downloadCommand).then(function () {
                                    logger.log("info", "Download command executed");
                                    sc.execute_command(comp[i].ssh_resource.installCommand).then(function () {
                                        logger.log("info", "Install command executed");
                                        sc.execute_command(comp[i].ssh_resource.configureCommand).then(function () {
                                            logger.log("info", "Configure command executed");
                                            sc.execute_command(comp[i].ssh_resource.startCommand).then(function () {
                                                logger.log("info", "Start command executed");
                                            }).catch(function (err) {
                                                logger.log("error", "Start command error " + err);
                                            });
                                        }).catch(function (err) {
                                            logger.log("error", "Configure command error " + err);
                                        });
                                    }).catch(function (err) {
                                        logger.log("error", "Install command error " + err);
                                    });
                                }).catch(function (err) {
                                    logger.log("error", "Download command error " + err);
                                });
                            }
                        }
                    }
                }
            }(comp, i));
        }


        //We collect all the started events, once they are all received we generate the flow skeleton based on the links
        bus.on('node-started', function (container_id, comp_name) {
            tmp++;
            //console.log(tmp + ' :: ' + nb);
            //Add container id to the component
            var comp = that.dep_model.find_node_named(comp_name);
            comp.container_id = container_id;

            if (tmp >= nb) {
                tmp = 0;

                var comp_tab = that.dep_model.get_all_hosted();
                //For all Node-Red hosted components we generate the websocket proxies
                for (var ct_elem of comp_tab) {
                    (function (comp_tab, ct_elem) {
                        if (ct_elem._type === 'node_red') {
                            var host_id = ct_elem.id_host;
                            var host = that.dep_model.find_node_named(host_id);

                            //Get all links that start from the component
                            var src_tab = that.dep_model.get_all_outputs_of_component(ct_elem);
                            //Get all links that end in the component
                            var tgt_tab = that.dep_model.get_all_inputs_of_component(ct_elem);

                            if ((src_tab.length > 0) || (tgt_tab.length > 0)) {
                                var noderedconnector = nodered_connector();
                                noderedconnector.getCurrentFlow(host.ip, ct_elem.port).then(function (the_flow) {
                                    that.generate_components(host.ip, ct_elem.port, src_tab, tgt_tab, that.dep_model, the_flow);
                                });
                            }
                        }
                    }(comp_tab, ct_elem));
                }
                return;
            }
        });
    };


    //This part has to be heavily refactored... Too late for this right now...
    that.generate_components = function (ip_host, tgt_port, src_tab, tgt_tab, dm, old_components) {
        //We keep the old elements without the generated ones
        var filtered_old_components = old_components.filter(function (elem) {
            if (elem.name !== undefined) {
                if (!elem.name.startsWith("to_") && !elem.name.startsWith("from_")) {
                    return elem;
                }
            }
        });

        var flow = '[';
        var dependencies = "";
        var tab = uuidv4();

        //For each link starting from the component we add a websocket out component
        for (var j in src_tab) {
            var tgt_component = dm.find_node_named(src_tab[j].target);
            var source_component = dm.find_node_named(src_tab[j].src);
            var tgt_host_id = tgt_component.id_host;
            var tgt_host = dm.find_node_named(tgt_host_id);
            var src_host_id = source_component.id_host;
            var src_host = dm.find_node_named(src_host_id);

            if (tgt_component._type === 'node_red' && source_component._type === 'node_red') {
                var client = uuidv4();
                flow += '{"id":"' + uuidv4() + '","type":"websocket out","z": "dac41de7.a03038","name":"to_' + tgt_component.name + '","server":"","client":"' + client + '","x":331.5,"y":237,"wires":[]},{"id":"' + client + '","type":"websocket-client","path":"ws://' + tgt_host.ip + ':' + tgt_component.port + '/ws/' + source_component.name + '","wholemsg":"false"},';
            } else {
                if (source_component._type === 'node_red') { //Check if we have a plugin for this type of component
                    if (tgt_component.nr_description !== undefined && tgt_component.nr_description !== "") {
                        for (w in tgt_component.nr_description.node) {
                            var _tmp = tgt_component.nr_description.node[w];
                            _tmp.z = "dac41de7.a03038";
                            if (_tmp.serialport !== undefined && _tmp.serialport !== "") {
                                _tmp.serialport = tgt_host.physical_port;
                            }
                            flow += JSON.stringify(_tmp) + ','; //how could we configure this?
                        }
                        if (tgt_component.nr_description.package !== undefined) {
                            dependencies = '{"module": "' + tgt_component.nr_description.package + '"}'; //What if several?
                        }
                    }
                }
            }
        }

        //For each link ending in the component we add a websocket in component
        for (var z in tgt_tab) {
            var server = uuidv4();
            var target_component = dm.find_node_named(tgt_tab[z].target);
            var src_component = dm.find_node_named(tgt_tab[z].src);
            if (src_component._type === 'node_red' && target_component._type === 'node_red') {
                flow += '{"id":"' + uuidv4() + '","type":"websocket in","z": "dac41de7.a03038","name":"from_' + src_component.name + '","server":"' + server + '","client":"","x":143.5,"y":99,"wires":[]},{"id":"' + server + '","type":"websocket-listener","path":"/ws/' + src_component.name + '","wholemsg":"false"},';
            }
        }

        //Remove the last ','
        flow = flow.slice(0, -1);
        //Close the flow description
        flow += ']';

        //We concat the old flow with the new one
        if (flow.length > 2) { // not empty "[]"
            var t = JSON.parse(flow);
            var result = filtered_old_components.concat(t)
            var nr_connector = nodered_connector();
            if (dependencies !== "") {
                nr_connector.installNodeType(ip_host, tgt_port, dependencies).then(function () {
                    nr_connector.setFlow(ip_host, tgt_port, JSON.stringify(result), tgt_tab, src_tab, dm);
                });
            } else {
                nr_connector.setFlow(ip_host, tgt_port, JSON.stringify(result), tgt_tab, src_tab, dm);
            }

        }
    }


    that.start = function () {

        that.webSocketServerObject.on('connection', function (socketObject) {
            that.socketObject = socketObject;

            //Send status info to the UI
            var nfier = notifier(that.socketObject);
            nfier.start();

            //Load component types from the repository
            var cl = class_loader();
            cl.findModules({
                folder: './repository'
            }, function (modules) {
                var tab = [];
                var mmodel = "";
                for (var j = 0; j < modules.length; j++) {
                    var tmp_ = {};
                    tmp_.id = modules[j].id.replace('.js', '');
                    var comp = modules[j].module({});
                    (comp.id_host === undefined) ? tmp_.isExternal = true: tmp_.isExternal = false;
                    tmp_.module = comp;
                    tab.push(tmp_);
                }
                that.socketObject.send("@" + JSON.stringify(tab));


                //Wait for a model from the editor
                socketObject.on('message', async function (message) {

                    //Create a deployment model
                    var dm = mm.deployment_model({});
                    //Add types to the registry before we create the instances
                    dm.type_registry = modules; //can be used as follows modules[i].module({})

                    //Load the model
                    logger.log("info", "Received model from the editor");
                    var data = JSON.parse(message);
                    dm.name = data.name;
                    dm.revive_components(data.components);
                    dm.revive_links(data.links);
                    if (dm.is_valid()) {

                        logger.log("info", "Model Loaded: " + JSON.stringify(dm.components));

                        //Compare model
                        var comparator = comparison_engine(that.dep_model);
                        that.diff = comparator.compare(dm);
                        that.dep_model = dm; //target model becomes current

                        //First do all the removal stuff - TODO refactor
                        logger.log("info", "Stopping removed containers");
                        await that.remove_containers(that.diff);

                        //Deploy only the added stuff
                        logger.log("info", "Starting deployment");
                        await that.run(that.diff);
                        //logger.log("info", "Deployment Completed");

                        //}
                    } else {
                        logger.log("info", "Model not loaded since not valid: " + JSON.stringify(dm.components));
                    }

                });
            });

            socketObject.on('close', function (c, d) {
                logger.log('info', 'Disconnect ' + c + ' -- ' + d);
            });

        });
    };

    return that;
}());


module.exports = engine;
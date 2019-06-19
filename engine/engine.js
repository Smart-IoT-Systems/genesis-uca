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
var model_observer = require('./runtime_observer.js');
var mqtt = require('mqtt');
var nodered_connector = require('./connectors/nodered_connector.js');
var fs = require('fs');

var engine = (function () {
    var that = {};

    that.available_types = [];
    that.modules = [];

    that.dep_model = mm.deployment_model({});
    that.diff = {};

    that.MQTTClient = {};

    that.graph = {};

    that.getDM_UI = function (req, res) {
        var all_in_one = {
            dm: that.dep_model,
            graph: that.graph
        };
        res.end(JSON.stringify(all_in_one));
    }

    that.getDM = function (req, res) {
        res.end(JSON.stringify(that.dep_model));
    }

    that.getTypes = function (req, res) {
        res.end(JSON.stringify(that.available_types));
    };

    that.remove_containers = async function (diff) {
        var removed = diff.list_of_removed_components;
        var connector = dc();
        if (removed.length === 0) {
            return;
        }
        for (var i in removed) {

            var host = diff.old_dm.find_host(removed[i]);

            if (host !== null) {
                //Need to find the host in the old model
                if (host._type === "/infra/docker_host") {
                    await connector.stopAndRemove(removed[i].container_id, host.ip, host.port);
                }
            }
        }
        bus.emit('remove-all');
    };

    that.deploy_agents = async function (links_deployer_tab) {
        return new Promise(async function (resolve, reject) {
            logger.log("info", "Starting deployment of deployment agents");
            var nb_deployers = 0;

            var map_host_agent = [];
            for (var l in links_deployer_tab) {
                var tgt_agent_name = that.dep_model.get_comp_name_from_port_id(links_deployer_tab[l].target);
                var tgt_agent = that.dep_model.find_node_named(tgt_agent_name);
                var host_agent_name = that.dep_model.get_comp_name_from_port_id(links_deployer_tab[l].src);
                var host_agent = that.dep_model.find_node_named(host_agent_name);
                var tgt_agent_host = that.dep_model.find_host(tgt_agent);
                var src_agent_host = that.dep_model.find_host(host_agent);

                var d_agent = agent(src_agent_host, tgt_agent_host, tgt_agent);
                await d_agent.prepare();
                var cont_id = await d_agent.install();
                tgt_agent.container_id = cont_id;
                map_host_agent[cont_id] = src_agent_host;
            }

            bus.on('d_agent_success', function (cfg) {
                nb_deployers++;
                var con_docker = dc();
                var c = that.dep_model.find_node_named(cfg);
                con_docker.stopAndRemove(c.container_id, map_host_agent[c.container_id].ip, map_host_agent[c.container_id].port).then(function () {
                    bus.emit('node-started', c.container_id, cfg);
                });
                if(nb_deployers >= links_deployer_tab.length){
                    resolve(true);
                }
            });

            bus.on('d_agent_error', function (cfg) {
                nb_deployers++;
                var con_docker = dc();
                var c = that.dep_model.find_node_named(cfg);
                con_docker.stopAndRemove(c.container_id, map_host_agent[c.container_id].ip, map_host_agent[c.container_id].port).then(function () {
                    bus.emit('node-error', c.container_id, cfg);
                });
                if(nb_deployers >= links_deployer_tab.length){
                    resolve(false);
                }
            });
        });
    };

    that.deploy_thingml = async function (comp, host) {
        //we should generate the plantuml
        var tcli = thingmlcli(comp);
        tcli.build("./generated_uml_" + comp.name, "uml").catch(function (err) {
            logger.log("error", err);
        });

        //Then we generate for the target
        tcli.build("./generated_" + comp.name, comp.target_language).then(function (elem) {
            //if java we need to build and deploy
            if (comp.target_language === 'java') {

                logger.log("info", process.cwd() + "/generated_" + comp.name);
                var mb = mvn_builder.create({
                    cwd: process.cwd() + "/generated_" + comp.name
                });
                mb.execute(['clean', 'install'], {
                    'skipTests': true
                }).then(function () {
                    //TODO: make it more generic
                    //as a start we connect and deploy via SSH
                    var sc = sshc(host.ip, host.port, comp.ssh_resource.credentials.username, comp.ssh_resource.credentials.password, comp.ssh_resource.credentials.sshkey);
                    sc.upload_file("./generated_" + comp.name + '/target/' + comp.name + '-1.0.0-jar-with-dependencies.jar', '/home/' + comp.ssh_resource.credentials.username + '/' + comp.name + '-1.0.0-jar-with-dependencies.jar').then(function (file_path_tgt) {
                        sc.execute_command(comp.ssh_resource.startCommand);
                        bus.emit('ssh-started', host.name);
                        bus.emit('ssh-started', comp.name);
                        bus.emit('node-started', "", comp.name);
                    }).catch(function (err) {
                        logger.log("error", err);
                    });
                }).catch(function (err) {
                    logger.log("error", "mvn clean install failed: " + err);
                });
            }
            if (comp[i].target_language === 'nodejs') {
                logger.log("info", process.cwd() + "/generated_" + comp.name);
                var sc = sshc(host.ip, host.port, comp.ssh_resource.credentials.username, comp.ssh_resource.credentials.password, comp.ssh_resource.credentials.sshkey);
                sc.upload_directory("./generated_" + comp[i].name, '/home/' + comp.ssh_resource.credentials.username + '/generated_' + comp.name).then(function (file_path_tgt) {
                    sc.execute_command(comp.ssh_resource.startCommand);
                    bus.emit('ssh-started', host.name);
                    bus.emit('ssh-started', comp.name);
                    bus.emit('node-started', "", comp.name);
                }).catch(function (err) {
                    logger.log("error", err);
                });;
            }
        }).catch(function (err) {
            logger.log("error", err);
        });
    };

    that.deploy_nodered = async function (comp, host) {
        var connector = dc();
        var docker_image_nr = "nicolasferry/multiarch-node-red-thingml:latest";
        if (comp.docker_resource.image !== docker_image_nr && comp.docker_resource.image !== "") {
            docker_image_nr = comp.docker_resource.image;
        }
        connector.buildAndDeploy(host.ip, host.port, comp.docker_resource.port_bindings, comp.docker_resource.devices, "", docker_image_nr, comp.docker_resource.mounts, comp.docker_resource.links, comp.name, host.name).then(function (id) {
            if ((comp.nr_flow !== undefined && comp.nr_flow !== "") ||
                (comp.path_flow !== "" && comp.path_flow !== undefined)) { //if there is a flow to load with the nodered node
                let noderedconnector = nodered_connector();
                var _data = "";
                if (comp.path_flow !== "" && comp.path_flow !== undefined) {
                    _data = fs.readFileSync(comp.path_flow);
                } else {
                    _data = JSON.stringify(comp.nr_flow);
                }
                noderedconnector.installAllNodeTypes(host.ip, comp.provided_communication_port[0].port_number, comp.packages).then(function () {
                    noderedconnector.setFlow(host.ip, comp.provided_communication_port[0].port_number, _data, [], [], that.dep_model).then(function () {
                        bus.emit('node-started', id, comp.name);
                    });
                });
            }
        }).catch(function (err) {
            logger.log('info', "Could not deploy node: " + comp.name + " => " + err);
        });
    };

    that.deploy_ssh = async function (comp, host) {
        var sc = sshc(host.ip, host.port, comp.ssh_resource.credentials.username, comp.ssh_resource.credentials.sshkey, comp.ssh_resource.credentials.sshkey);
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
    };


    that.deploy_one_component = async function (compo) { //We wrap in a closure so that each comp deployment comes with its own context
        //if not to be deployed by a deployment agent
        if (!that.dep_model.need_deployment_agent(compo)) {
            var host = that.dep_model.find_host(compo);
            //And if there is an host to deploy on
            if (host !== undefined) {
                //Manage ThingML nodes
                if (compo._type === "/internal/thingml") {
                    await that.deploy_thingml(compo, host);
                } else {
                    //Manage component on docker
                    if (host._type === "/infra/docker_host") {

                        //Manage Node-red on Docker
                        if (compo._type === "/internal/node_red") {
                            await that.deploy_nodered(compo, host);
                        } else {
                            //Manage simple docker
                            var connector = dc();
                            var id = await connector.buildAndDeploy(host.ip, host.port, compo.docker_resource.port_bindings, compo.docker_resource.devices, compo.docker_resource.command, compo.docker_resource.image, compo.docker_resource.mounts, compo.docker_resource.links, compo.name, host.name);
                            bus.emit('node-started', id, compo.name);
                        }
                    }
                    //Manage component via Ansible
                    if (compo.ansible_resource.playbook_path !== "" && compo.ansible_resource.playbook_path !== undefined) {
                        var connector = ac(host, ccompo);
                        connector.executePlaybook();
                    }

                    //Manage component via ssh
                    if (compo.ssh_resource.credentials.sshkey !== "") {
                        await that.deploy_ssh(compo, host);
                    }
                }
            }
        }
    };




    that.run = function (diff) { //TODO: factorize
        return new Promise(async function (resolve, reject) {
            var comp = diff.list_of_added_hosted_components;
            var nb = that.dep_model.get_all_hosted().length;
            var tmp = 0;
            var nb_link = that.dep_model.links.length;
            var tmp_link = 0;

            //Deployment agent
            var links_deployer_tab = diff.list_of_added_links_deployer;
            if (links_deployer_tab.length > 0) {
                await that.deploy_agents(links_deployer_tab);
            }

            if (comp.length === 0 && diff.list_of_added_links.length === 0) { //No new component then and no new links, we are done
                resolve(0);
            }


            bus.on('node-error', function (container_id, comp_name) {
                tmp++;
                if (tmp >= nb) {
                    tmp = 0;

                    var comp_tab = that.dep_model.get_all_hosted();

                    manage_links(comp_tab);
                }
            });

            //We collect all the started events, once they are all received we generate the flow skeleton based on the links
            bus.on('node-started', function (container_id, comp_name) {
                tmp++;
                //Add container id to the component
                var comp = that.dep_model.find_node_named(comp_name);
                comp.container_id = container_id;

                if (tmp >= nb) {
                    tmp = 0;
                    var comp_tab = that.dep_model.get_all_hosted();
                    manage_links(comp_tab);
                }
            });

            bus.on('link-done', function () {
                tmp_link++;
                console.log(tmp_link + "::" + nb_link);
                if (tmp_link >= nb_link) {
                    tmp_link = 0;
                    resolve(tmp_link);
                }
            });

            var compo_already_deployed = [];

            //Other nodes
            for (var i in comp) {
                //TODO: make this recursive!
                var comp_mandatories = that.dep_model.get_all_mandatory_of_a_component(comp[i]);
                if (comp_mandatories !== null) {
                    for (var m in comp_mandatories) {
                        compo_already_deployed[comp_mandatories[m].name] = true;
                        await that.deploy_one_component(comp_mandatories[m]);
                    }
                }
                if (compo_already_deployed[comp[i].name] === undefined) {
                    compo_already_deployed[comp[i].name] = true;
                    (function (one_component) {
                        that.deploy_one_component(one_component);
                    }(comp[i]));
                }
            }

            var manage_links = function (comp_tab) {
                //For all Node-Red hosted components we generate the websocket proxies
                for (var ct_elem of comp_tab) {
                    (function (comp_tab, ct_elem) {
                        if (ct_elem._type === '/internal/node_red') {
                            var host = that.dep_model.find_host(ct_elem);

                            //Get all links that start from the component
                            var src_tab = that.dep_model.get_all_outputs_of_component(ct_elem);
                            //Get all links that end in the component
                            var tgt_tab = that.dep_model.get_all_inputs_of_component(ct_elem);

                            if ((src_tab.length > 0) || (tgt_tab.length > 0)) {
                                var noderedconnector = nodered_connector();
                                noderedconnector.getCurrentFlow(host.ip, ct_elem.provided_communication_port[0].port_number).then(function (the_flow) {
                                    that.generate_components(host.ip, ct_elem.provided_communication_port[0].port_number, src_tab, tgt_tab, that.dep_model, the_flow);
                                    bus.emit('link-done');
                                });
                            } else {
                                bus.emit('link-done');
                            }
                        } else {
                            bus.emit('link-done');
                        }
                    }(comp_tab, ct_elem));
                }
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
            } else {
                if (elem.id !== undefined) {
                    return elem;
                }
            }
        });

        var flow = '[';
        var dependencies = "";
        var tab = uuidv4();

        //For each link starting from the component we add a websocket out component
        for (var j in src_tab) {
            var a_name = dm.get_comp_name_from_port_id(src_tab[j].target);
            var b_name = dm.get_comp_name_from_port_id(src_tab[j].src);
            var tgt_component = dm.find_node_named(a_name);
            var source_component = dm.find_node_named(b_name);
            var tgt_host = dm.find_host(tgt_component);
            var src_host = dm.find_host(source_component);

            if (tgt_component._type === '/internal/node_red' && source_component._type === '/internal/node_red') {
                var client = uuidv4();
                flow += '{"id":"' + uuidv4() + '","type":"websocket out","z": "dac41de7.a03038","name":"to_' + tgt_component.name + '","server":"","client":"' + client + '","x":331.5,"y":237,"wires":[]},{"id":"' + client + '","type":"websocket-client","path":"ws://' + tgt_host.ip + ':' + tgt_component.provided_communication_port[0].port_number + '/ws/' + source_component.name + '","wholemsg":"false"},';
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
            var a = dm.get_comp_name_from_port_id(tgt_tab[z].target);
            var b = dm.get_comp_name_from_port_id(tgt_tab[z].src);
            var target_component = dm.find_node_named(a);
            var src_component = dm.find_node_named(b);
            if (src_component._type === '/internal/node_red' && target_component._type === '/internal/node_red') {
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

    that.deploy = async function (req, res) {
        //Create a deployment model
        var dm = mm.deployment_model({});
        //Add types to the registry before we create the instances
        dm.type_registry = that.modules; //can be used as follows modules[i].module({})

        //Load the model
        logger.log("info", "Received model from the editor " + JSON.stringify(req.body));
        var d = req.body;
        var data;
        if (d.dm !== undefined) {
            data = d.dm;
            that.graph = d.graph;
        } else {
            data = d;
        }
        dm.name = data.name;
        dm.revive_components(data.components);
        logger.log("info", "Revive Comp");
        dm.revive_links(data.links);
        logger.log("info", "Revive Link");
        dm.revive_containments(data.containments);
        logger.log("info", "Revive Containment");

        if (dm.is_valid()) {

            logger.log("info", "Model Loaded: " + JSON.stringify(dm.components));

            //Compare model
            var comparator = comparison_engine(that.dep_model);
            that.diff = comparator.compare(dm);
            that.dep_model = dm; //target model becomes current

            //We start the model observer
            var m_observer = model_observer(that.dep_model);
            m_observer.start();

            //First do all the removal stuff - TODO refactor
            logger.log("info", "Stopping removed containers");
            await that.remove_containers(that.diff);

            //Deploy only the added stuff
            logger.log("info", "Starting deployment");
            that.run(that.diff).then(function () {
                res.end(JSON.stringify({
                    success: that.dep_model
                }));
                logger.log("info", "Deployment completed!");
            });
        } else {
            logger.log("info", "Model not loaded since not valid: " + JSON.stringify(dm));
            logger.log("error", "List of errors: " + JSON.stringify(dm.is_valid_with_errors()));
            res.end(JSON.stringify({
                error: "Model not loaded since not valid"
            }));
        }
    }


    that.start = function () {

        //We use MQTT for the notifications
        that.MQTTClient = mqtt.connect('ws://127.0.0.1:9001');
        var nfier = notifier(that.MQTTClient);
        nfier.start();

        //Load component types from the repository
        var cl = class_loader();
        cl.findModules({
            folder: './repository'
        }, function (modules) {
            var tab = [];
            for (var j = 0; j < modules.length; j++) {
                var tmp_ = {};
                tmp_.id = modules[j].id.replace('.js', '');
                var comp = modules[j].module({});
                (comp._type.indexOf('external') > -1) ? tmp_.isExternal = true: tmp_.isExternal = false;
                tmp_.module = comp;
                tab.push(tmp_);
            }
            that.available_types = tab;
            that.modules = modules;
        });
    };

    return that;
}());


module.exports = engine;
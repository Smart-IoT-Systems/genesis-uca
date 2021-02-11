var AvailabilityManager = require("./availability");
var mm = require('../metamodel/allinone.js');
var dc = require('./connectors/docker-connector.js');
var sshc = require('./connectors/ssh-connector.js');
var bus = require('./event-bus.js');
var uuidv4 = require('uuid/v4');
var comparison_engine = require('./model-comparison.js');
var class_loader = require('./class-loader.js');
var agent = require('./deployment-agent.js');
var monitor_agent = require('./monitoring-agent.js');
var logger = require('./logger.js');
var ac = require('./connectors/ansible-connector.js');
var thingmlcli = require('./thingml-compiler.js');
var mvn_builder = require('maven');
var notifier = require('./notifier');
var model_observer = require('./runtime_observer.js');
var mqtt = require('mqtt');
var nodered_connector = require('./connectors/nodered_connector.js');
var fs = require('fs');
var temp = require("temp");
var tar = require("tar");




var engine = (function () {
	var that = {};

    that.availabilityManager = new AvailabilityManager();
    
	that.available_types = [];
	that.modules = [];

	that.dep_model = mm.deployment_model({});
	that.diff = {};
	that.target_model = mm.deployment_model({});

	that.MQTTClient = {};

	that.graph = {};

	that.m_observer = null;

	that.compo_already_deployed = [];
	that.compo_already_started = [];

	that.map_host_agent = [];

	that.getDM_UI = function (req, res) {
		var all_in_one = {
			dm: that.dep_model,
			graph: that.graph
		};
		res.end(JSON.stringify(all_in_one));
	}

	that.getRuntime_info = function (req, res) {
		//Update runtime info
		that.dep_model.components.forEach(element => {
			bus.emit('runtime-info', element.name, element._runtime.Status);
		});
		res.end(JSON.stringify(that.dep_model));
	}

	that.getDM = function (req, res) {
		res.end(JSON.stringify(that.dep_model));
	}

	that.getTypes = function (req, res) {
		res.end(JSON.stringify(that.available_types));
	};

	that.get_targetDM = function (req, res) {
		res.end(JSON.stringify(that.target_model));
	};

    that.update_component = function (req, res) {
	logger.info("UPDATE COMPONENT!");
		var input = req.body;
		logger.log("info", "Received request to update Target model in memory " + JSON.stringify(input));
		try {
			that.target_model.change_attribute(input.name, input.attribute, input.value);

		} catch (e) {
			logger.log("error", "Body not valid");
			res.end("error");
		}
		res.end("OK");
	};

    
	that.push_model = function (req, res) {
	    logger.info("PUSH MODEL!");
	    req.body = that.target_model;
		that.deploy(req, res);
	};


    that.update_target_model = function (req, res) {
	logger.info("UPDATE TARGET MODEL!");
		let data = req.body;
		that.target_model = mm.deployment_model({});
		that.target_model.name = data.name;
		that.target_model.revive_components(data.components);
		that.target_model.revive_links(data.links);
		that.target_model.revive_containments(data.containments);

		//Update runtime info
		that.dep_model.components.forEach(element => {
			if (that.target_model.find_node_named(element.name) !== undefined) {
				bus.emit('runtime-info', element.name, element._runtime.Status);
			}
		});

		res.end(JSON.stringify(that.target_model));
	};

	that.to_be_removed = function (diff, comp) {
	    logger.info("TO BE REMOVED!");
	    var result = false;
		var removed_comp = diff.list_of_removed_components;
		for (var i in removed_comp) {
			if (removed_comp[i].name === comp.name) {
				result = true;
				return result;
			}
		}
		return result;
	};

	that.remove_one_component = async function (cmpt, diff) {
	    logger.info("REMOVE ONE COMPONENT!");
	    var host = diff.old_dm.find_host_one_level_down(cmpt);
	    var device_host = diff.old_dm.find_host(cmpt);
	    
	    if (host !== null) {
		//Need to find the host in the old model
		if (host._type === "/infra/docker_host") {
		    await connector.stopAndRemove(cmpt.container_id, device_host.ip, host.port);

		} else if (cmpt._type === "/internal/node_red_flow") {
		    if (!that.to_be_removed(diff, host)) {
			var n_connector = nodered_connector();
			/*n_connector.setFlow(host.ip, removed[i].required_communication_port[0].port_number, "[]", [], [], that.dep_model).then(function () {
			  logger.log("info", "Node-Red Flow Removed!");
			  });*/
			logger.log("info", "Host " + host.name);
			await n_connector.setFlow(device_host.ip,
						  cmpt.required_communication_port[0].port_number,
						  "[]",
						  [],
						  [],
						  that.dep_model);
			logger.log("info", "Node-Red Flow Removed!");
		    }
		    
		} else if (that.need_ssh(cmpt)) {
		    var ssh_connection = sshc(device_host.ip,
					      host.port,
					      cmpt.ssh_resource.credentials.username,
					      cmpt.ssh_resource.credentials.password,
					      cmpt.ssh_resource.credentials.sshkey,
					      cmpt.ssh_resource.credentials.agent);
		    await ssh_connection.execute_command(cmpt.ssh_resource.stopCommand);
		}
	    }
	};
    
	that.remove_containers = async function (diff) {
		var removed = diff.list_of_removed_components;
		var removed_hosts = diff.list_removed_hosts;

		if (removed.length === 0 && removed_hosts.length <= 0) {
			return;
		}
		var dhs = []; // Docker host
		var nrfs = []; // NodeRED
		var sshs = []; // Using SSH
		for (var i in removed) {

			/*if (diff.old_dm.is_top_component(removed[i])) {
		(function (one_component, d) {
		    that.recursive_remove(one_component, d);
		}(removed[i], diff));
	    }*/
			var host = diff.old_dm.find_host_one_level_down(removed[i]);
			var device_host = diff.old_dm.find_host(removed[i]);

			if (host !== null) {
				//Need to find the host in the old model
				if (host._type === "/infra/docker_host") {
					dhs.push(removed[i]);
					//await connector.stopAndRemove(removed[i].container_id, device_host.ip, host.port);
				} else if (removed[i]._type === "/internal/node_red_flow") {
					if (!that.to_be_removed(diff, host)) {
						nrfs.push(removed[i]);
						/*var n_connector = nodered_connector();
						logger.log("info", "Host "+host.name);
						await n_connector.setFlow(device_host.ip, removed[i].required_communication_port[0].port_number, "[]", [], [], that.dep_model);
						logger.log("info", "Node-Red Flow Removed!");*/
					}
				} else if (that.need_ssh(removed[i]) && !removed[i].availability.isReplicated()) {
					sshs.push(removed[i]);
					//var ssh_connection = sshc(device_host.ip, host.port, removed[i].ssh_resource.credentials.username, removed[i].ssh_resource.credentials.password, removed[i].ssh_resource.credentials.sshkey, removed[i].ssh_resource.credentials.agent);
					//await ssh_connection.execute_command(removed[i].ssh_resource.stopCommand);
				}
			}
		}

		var get_dhs = async () => {
			await Promise.all(dhs.map(async (item) => {
				var host = diff.old_dm.find_host_one_level_down(item);
				var device_host = diff.old_dm.find_host(item);
				var connector = dc();
				bus.emit('removed', item.name);
				await connector.stopAndRemove(item.container_id, device_host.ip, host.port)
			}));
		}

		var get_nrfs = async () => {
			await Promise.all(nrfs.map(async (item) => {
				var host = diff.old_dm.find_host_one_level_down(item);
				var device_host = diff.old_dm.find_host(item);
				var n_connector = nodered_connector();
				logger.log("info", "Host " + host.name);
				await n_connector.setFlow(device_host.ip, item.required_communication_port[0].port_number, "[]", [], [], that.dep_model);
				logger.log("info", "Node-Red Flow Removed!");
			}));
		}

		var get_sshs = async () => {
			await Promise.all(sshs.map(async (item) => {
				var host = diff.old_dm.find_host_one_level_down(item);
				var device_host = diff.old_dm.find_host(item);
				var ssh_connection = sshc(device_host.ip, host.port, item.ssh_resource.credentials.username, item.ssh_resource.credentials.password, item.ssh_resource.credentials.sshkey, item.ssh_resource.credentials.agent);
				await ssh_connection.execute_command(item.ssh_resource.stopCommand);
			}));
		}

		await get_sshs();
		await get_nrfs();
		await get_dhs();

		for (var j in removed_hosts) {
			if (removed_hosts[j]._type.indexOf('infra') >= 0) { //Only infra have monitoring agents so far
				if (removed_hosts[j].monitoring_agent !== undefined && removed_hosts[j].monitoring_agent !== "none") {
					var monitor = monitor_agent(removed_hosts[j], "full");
					await monitor.remove();
				}
			}
		}

		bus.emit('remove-all');
	};

	that.deploy_agents = async function (links_deployer_tab) {
		return new Promise(async function (resolve, reject) {
			bus.removeAllListeners('d_agent_success');
			logger.log("info", "Starting deployment of deployment agents");
			var nb_deployers = 0;

			let port_for_mapping = 1889;

			for (var l in links_deployer_tab) {
				var tgt_agent_name = that.dep_model.get_comp_name_from_port_id(links_deployer_tab[l].target);
				var tgt_agent = that.dep_model.find_node_named(tgt_agent_name);
				var host_agent_name = that.dep_model.get_comp_name_from_port_id(links_deployer_tab[l].src);
				var host_agent = that.dep_model.find_node_named(host_agent_name);
				var tgt_agent_host = that.dep_model.find_host(tgt_agent);
				var src_agent_host = that.dep_model.find_host(host_agent);

				var d_agent = agent(src_agent_host, tgt_agent_host, tgt_agent);
				await d_agent.prepare();
				var cont_id = await d_agent.install(port_for_mapping);
				bus.emit('host-config', "", tgt_agent_host.name);
				that.map_host_agent[cont_id] = src_agent_host;
				tgt_agent.container_id = cont_id;
				port_for_mapping++;
			}

			bus.on('d_agent_success', function (cfg) {
				nb_deployers++;
				var con_docker = dc();
				var c = that.dep_model.find_node_named(cfg);
				con_docker.stopAndRemove(c.container_id, that.map_host_agent[c.container_id].ip, that.map_host_agent[c.container_id].port).then(function () {
					bus.emit('node-started', c.container_id, cfg);
				});
				if (nb_deployers >= links_deployer_tab.length) {
					resolve(true);
				}
			});

			bus.on('d_agent_error', function (cfg) {
				nb_deployers++;
				var con_docker = dc();
				var c = that.dep_model.find_node_named(cfg);
				con_docker.stopAndRemove(c.container_id, that.map_host_agent[c.container_id].ip, that.map_host_agent[c.container_id].port).then(function () {
					bus.emit('node-error', c.container_id, cfg);
				});
				if (nb_deployers >= links_deployer_tab.length) {
					resolve(false);
				}
			});
		});
	};

	that.monitoring_agents = async function (comps) {
		logger.log("info", "Starting deployment of monitoring agents ");
		for (var inf in comps) {
			if (comps[inf]._type.indexOf('infra') > 0) {
				if (comps[inf].monitoring_agent !== undefined) {
					if (comps[inf].monitoring_agent !== 'none') {
						var monitor = monitor_agent(comps[inf], "full");
						await monitor.install();
					}
				}
			}
		}
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

				//if smool_kp then we need to move the config repo
				if (comp._type.indexOf("smoolkp") > 0) {
					let config_dir = process.cwd() + "/generated_" + comp.name + "/src/main/resources";
					try {
						if (!fs.existsSync(config_dir)) {
							fs.mkdirSync(config_dir);
							fs.mkdirSync(config_dir + "/config");
						}
						fs.renameSync(process.cwd() + "/generated_" + comp.name + "/src/main/java/config/mapping.properties", config_dir + "/config/mapping.properties", function (err) {
							if (err) throw err;
						})
					} catch (err) {
						console.error(err);
					}
				}

				var mb = mvn_builder.create({
					cwd: process.cwd() + "/generated_" + comp.name
				});
				mb.execute(['clean', 'install'], {
					'skipTests': true
				}).then(function () {
					//TODO: make it more generic
					//as a start we connect and deploy via SSH
					var sc = sshc(host.ip, "22", comp.ssh_resource.credentials.username, comp.ssh_resource.credentials.password, comp.ssh_resource.credentials.sshkey, comp.ssh_resource.credentials.agent);
					sc.upload_file("./generated_" + comp.name + '/target/' + comp.config_name + '-1.0.0-jar-with-dependencies.jar', '/home/' + comp.ssh_resource.credentials.username + '/' + comp.config_name + '-1.0.0-jar-with-dependencies.jar').then(function (file_path_tgt) {
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
			if (comp.target_language === 'nodejs') {
				logger.log("info", process.cwd() + "/generated_" + comp.name);
				var sc = sshc(host.ip, host.port, comp.ssh_resource.credentials.username, comp.ssh_resource.credentials.password, comp.ssh_resource.credentials.sshkey, comp.ssh_resource.credentials.agent);
				sc.upload_directory("./generated_" + comp.name, '/home/' + comp.ssh_resource.credentials.username + '/generated_' + comp.name).then(function (file_path_tgt) {
					bus.emit('ssh-started', host.name);
					sc.execute_command(comp.ssh_resource.startCommand).then(function () {
						bus.emit('ssh-started', comp.name);
						bus.emit('node-started', "", comp.name);
					});
				}).catch(function (err) {
					logger.log("error", err);
				});;
			}
		}).catch(function (err) {
			logger.log("error", err);
		});
	};

	that.deploy_nodered = async function (comp, host) {
		return new Promise(function (resolve, reject) {
			var connector = dc();
			var docker_image_nr = "nicolasferry/multiarch-node-red-thingml:latest";
			if (comp.docker_resource.image !== docker_image_nr && comp.docker_resource.image !== "") {
				docker_image_nr = comp.docker_resource.image;
			}
		    connector.buildAndDeploy(host.ip,
					     host.port,
					     comp.docker_resource.port_bindings,
					     comp.docker_resource.devices,
					     "",
					     docker_image_nr,
					     comp.docker_resource.mounts,
					     comp.docker_resource.links,
					     comp.name,
					     host.name,
					     comp.docker_resource.environment)
			.then(function (id) {
			    if ((comp.nr_flow !== undefined && comp.nr_flow !== "") ||
				(comp.path_flow !== "" && comp.path_flow !== undefined)) { //if there is a flow to load with the nodered node
				let noderedconnector = nodered_connector();
				var _data = "";
				if (comp.path_flow !== "" && comp.path_flow !== undefined) {
				    _data = fs.readFileSync(comp.path_flow);

				} else {
				    _data = JSON.stringify(comp.nr_flow);

				}
				noderedconnector.installAllNodeTypes(host.ip,
								     comp.provided_communication_port[0].port_number,
								     comp.packages)
				    .then(function () {
					noderedconnector.setFlow(host.ip,
								 comp.provided_communication_port[0].port_number,
								 _data,
								 [],
								 [],
								 that.dep_model)
					    .then(function () {
						resolve(comp.name);
						bus.emit('node-started', id, comp.name);
					    });
				    });
			    }
			}).catch(function (err) {
			    logger.log('info', "Could not deploy node: " + comp.name + " => " + err);
			    reject(err);
			});
		});
	};
    
    
    /**
     * Deploy a component using SSH
     */
    that.deploy_ssh = function (component, host) {
	return new Promise(async function (resolve, reject) {

	    var ssh_port = host.port;
	    if (host._type === "/infra/docker_host") {
		ssh_port = "22";
	    }

	    if (component.availability.isReplicated()) {
		try {
		    that.availabilityManager.handle(component, host);

		} catch (error) {
		    logger.error(error.message);
		    
		}
	    } else {
		var sc = sshc(host.ip,
			      ssh_port,
			      component.ssh_resource.credentials.username,
			      component.ssh_resource.credentials.password,
			      component.ssh_resource.credentials.sshkey,
			      component.ssh_resource.credentials.agent);
		
 		// Just for fun 0o' let's try the most crappy code ever!
		// Actually this is not fun :'(
		
		let src_upload = component.ssh_resource.uploadCommand[0];
		let tgt_upload = component.ssh_resource.uploadCommand[1];
		
		sc.upload_file(src_upload, tgt_upload).then(function () {
		    logger.info("Upload command executed");
		    sc.execute_command(component.ssh_resource.downloadCommand).then(function () {
			logger.info("Download command executed");
			sc.execute_command(component.ssh_resource.installCommand).then(function () {
			    logger.info("Install command executed");
			    sc.execute_command(component.ssh_resource.configureCommand).then(function () {
				logger.info("Configure command executed");
				sc.execute_command(component.ssh_resource.startCommand).then(function () {
				    logger.info("Start command executed");
				    bus.emit('ssh-started', host.name);
				    bus.emit('ssh-started', component.name);
				    bus.emit('node-started', "", component.name);
				    resolve(true);
				    
				}).catch(function (err) {
				    logger.log("error", "Start command error " + err);
				    reject(err);
				    
				});
			    }).catch(function (err) {
				logger.log("error", "Configure command error " + err);
				reject(err);
				
			    });
			}).catch(function (err) {
			    logger.log("error", "Install command error " + err);
			    reject(err);
			    
			});

		    }).catch(function (err) {
			logger.log("error", "Download command error " + err);
			reject(err);
			
		    });
		    
		}).catch(function (err) {
		    logger.log("error", "Upload command error " + err);
		    reject(err);
		    
		});
	    }
	});
    };
    
    
	that.deploy_node_red_flow = async function (a_component) {
	    return new Promise(function (resolve, reject) {
			bus.emit('container-config', a_component.name);
			var nredconnector = nodered_connector();
			var host = that.dep_model.find_host(a_component);
			var _data = "";
			if (a_component.path_flow !== "" && a_component.path_flow !== undefined) {
				_data = fs.readFileSync(a_component.path_flow);
			} else {
				_data = JSON.stringify(a_component.nr_flow);
			}
			nredconnector.installAllNodeTypes(host.ip, a_component.required_communication_port[0].port_number, a_component.packages).then(function () {
				nredconnector.setFlow(host.ip, a_component.required_communication_port[0].port_number, _data, [], [], that.dep_model).then(function () {
					bus.emit('node-started', null, a_component.name);
					resolve(a_component.name);
				});
			}).catch(function (err) {
				logger.log('info', "Could not deploy node: " + comp.name + " => " + err);
				reject(err);
			});;
		});
	};

    
    that.deploy_docker = async function (component, host) {
	if (component.docker_resource.image !== "") {
	    
	    if (component.availability.isReplicated()) {
		that.availabilityManager.handle(component, host);

	    } else {
		var connector = dc();
		var id = await connector.buildAndDeploy(
		    host.ip,
		    host.port,
		    component.docker_resource.port_bindings,
		    component.docker_resource.devices,
		    component.docker_resource.command,
		    component.docker_resource.image,
		    component.docker_resource.mounts,
		    component.docker_resource.links,
		    component.name,
		    host.name,
		    component.docker_resource.environment);
	    }
	}
	bus.emit('node-started', id, component.name);
    }



    /*
     * Deploy a component. Its dependencies and host should be
     * previously deployed (see method 'recursive_deploy').
     */
    that.deploy_one_component = async function (compo) { // We wrap in a closure so that each comp deployment comes with its own context
	//Functions provided by the component itself
	if (compo._configure !== undefined) {
	    await compo._configure();
	}
	
	// if not to be deployed by a deployment agent
	if (!that.dep_model.need_deployment_agent(compo)) {
	    var host = that.dep_model.find_host_one_level_down(compo);
	    
	    //And if there is an host to deploy on
	    if (host !== undefined) {
		
		//Manage ThingML nodes
		if (compo._type.startsWith("/internal/thingml")) {
		    await that.deploy_thingml(compo, host);
		    
		} else {
		    //Manage component on docker
		    if (host._type === "/infra/docker_host") {
			
			//Manage Node-red on Docker
			if (compo._type === "/internal/node_red") {
			    await that.deploy_nodered(compo, host);
			    
			} else {
			    that.deploy_docker(compo, host)
			}
		    }
		    
		    //Manage node-red-flow components
		    if (compo._type === "/internal/node_red_flow") {
			logger.log('info', 'Deploy a flow');
			await that.deploy_node_red_flow(compo);
		    }
		    
		    //Manage component via Ansible
		    if (compo.ansible_resource.playbook_path !== "" && compo.ansible_resource.playbook_path !== undefined) {
			var connector = ac(host, ccompo);
			connector.executePlaybook();
		    }
		    
		    //Manage component via ssh
		    if (that.need_ssh(compo)) {
			logger.log('info', 'Deploy via SSH ' + compo.name);
			await that.deploy_ssh(compo, host);
		    }
		}
	    }
	}
    };

    that.need_ssh = function (compo) {
		if ((compo.ssh_resource.credentials.sshkey !== undefined && compo.ssh_resource.credentials.sshkey !== "") ||
			(compo.ssh_resource.credentials.agent !== undefined && compo.ssh_resource.credentials.agent !== "") ||
			(compo.ssh_resource.credentials.password !== undefined && compo.ssh_resource.credentials.password !== "")) {
			if ((compo.ssh_resource.startCommand !== undefined && compo.ssh_resource.startCommand !== "") ||
				(compo.ssh_resource.configureCommand !== undefined && compo.ssh_resource.configureCommand !== "") ||
				(compo.ssh_resource.installCommand !== undefined && compo.ssh_resource.installCommand !== "")) {
				return true;
			} else {
				return false;
			}
		}

		return false;
	};


	/*
	 * Deploy a component and its dependencies, recursively.
	 *
	 * It traverses the 'graph' of components from the 'top one'. It
	 * first deploys its host and then its mandatory dependencies.
	 *
	 * Note: The property 'compo_already_deployed' is created
	 * dynamically at the end of the 'run' method.
	 */
	that.recursive_deploy = async function (cpnt) {

		/* Find the host of the given component to deploy, and check
		 * if it has been added or modified.
		 */
		var one_level = that.dep_model.find_host_one_level_down(cpnt);
		var one_level_new = false;
		for (var elem of that.diff.list_of_added_components) {
			if (elem.name === one_level.name) {
				one_level_new = true;
			}
		}

		// Deploy its 'host', if not yet deployed
		if ((one_level !== null) &&
			(that.compo_already_deployed[one_level] !== true) &&
			(one_level._type.indexOf('infra') < 0) &&
			one_level_new) {
			await that.recursive_deploy(one_level);
		}

		// Deploy the mandatory dependencies, if not yet deployed
		var comp_mandatories = that.dep_model.get_all_mandatory_of_a_component(cpnt);
		if (comp_mandatories !== null) {
			for (var m in comp_mandatories) {
				that.compo_already_deployed[comp_mandatories[m].name] = true;
				await that.deploy_one_component(comp_mandatories[m]); // Unclear, why not recursively?
			}
		}

		// Deploy the 'cpnt' component
		if (that.compo_already_deployed[cpnt.name] === undefined) {
			that.compo_already_deployed[cpnt.name] = true;
			await that.deploy_one_component(cpnt);
		}
	};


	/* Seems to deploy all the components that have been detected as
	 * 'addition' in the given 'diff', At least, those labelled as
	 * "added hosted". Seems to be just the added component (looking
	 * at model-comparison.js).
	 */
    that.run = function (diff) { //TODO: factorize
	logger.info("RUN!");
		return new Promise(async function (resolve, reject) {
			bus.removeAllListeners('node-error2');
			bus.removeAllListeners('node-started2');
			bus.removeAllListeners('link-done');

		    var comp = diff.list_of_added_hosted_components;
		    logger.info(diff.list_of_added_hosted_components.map(c => c.name));
			var tmp = 0;
			var tmp_link = 0;

			bus.on('node-error2', function (container_id, comp_name) {
				tmp++;
				if (tmp >= comp.length) {
					tmp = 0;

					manage_links(diff.list_of_added_hosted_components);
				} 
			});

			//We collect all the started events, once they are all received we generate the flow skeleton based on the links
			bus.on('node-started2', function (container_id, comp_name) {
				tmp++;
				//Add container id to the component
				console.log("Started node: " + tmp + " :::: " + comp.length);
				var compon = that.dep_model.find_node_named(comp_name);
				compon.container_id = container_id;

				if (tmp >= comp.length) {
					tmp = 0;
					manage_links(diff.list_of_added_hosted_components);
				}
			});

			bus.on('link-done', function () {
				tmp_link++;
				console.log("Link done: " + tmp_link + " :::: " + diff.list_of_added_links.length);
				if (tmp_link >= diff.list_of_added_links.length) {
					tmp_link = 0;
					resolve(tmp_link);
				}
			});

			//Deployment agent
			var links_deployer_tab = diff.list_of_added_links_deployer;
			if (links_deployer_tab.length > 0) {
				await that.deploy_agents(links_deployer_tab);
			}

			//Monitoring agents
			await that.monitoring_agents(diff.list_of_added_hosts);

			if (comp.length === 0 && diff.list_of_added_links.length === 0) { //No new component then and no new links, we are done
				resolve(0);
			}

			var manage_links = function (comp_tab) {
				//For all Node-Red hosted components we generate the websocket proxies
				/*for (var ct_elem of comp_tab) {
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
				                    bus.emit'link-done');
				                });
				            } else {
				                bus.emit('link-done');
				            }
				        } else {
				            bus.emit('link-done');
				        }
				    }(comp_tab, ct_elem));
				}*/
				resolve(0);
			}


			that.compo_already_deployed = [];

			for (var i in comp) {
				//await that.recursive_deploy(comp[i]);
				if (that.dep_model.is_top_component(comp[i])) {
					(function (one_component) {
						that.recursive_deploy(one_component);
					}(comp[i]));
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
		that.target_model = mm.deployment_model({});

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

		// Build the deployment model from the JSON fragments from the
		// HTTP POST requests.
		dm.name = data.name;
		dm.revive_components(data.components);
		logger.log("info", "Revive Comp");
		dm.revive_links(data.links);
		logger.log("info", "Revive Link");
		dm.revive_containments(data.containments);
		logger.log("info", "Revive Containment");

		// We keep a copy in memory of the model that has been pushed
		// last as a buffer for edit
		that.target_model.type_registry = that.modules;
		that.target_model.name = data.name;
		that.target_model.revive_components(data.components);
		that.target_model.revive_links(data.links);
		that.target_model.revive_containments(data.containments);

		if (dm.is_valid()) {

			res.end(JSON.stringify({
				started: that.dep_model
			}));

			that.already_deployed = [];

			// Compare models
			var comparator = comparison_engine(that.dep_model);
			that.diff = comparator.compare(dm);
			that.dep_model = dm; //target model becomes current

			// We set the model observer
			that.m_observer.set_model(that.dep_model);

			// First do all the removal stuff - TODO refactor
			logger.log("info", "Stopping removed containers");
			await that.remove_containers(that.diff);

			// Deploy only the added stuff
			logger.log("info", "Starting deployment");
			that.run(that.diff)
				.then(function () {
					bus.emit('deployment-completed');
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

		//We start the model observer
		that.m_observer = model_observer(that.dep_model);
		that.m_observer.start();

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

var Docker = require('dockerode');
var bus = require('../event-bus.js');
var logger = require('../logger.js');

var docker_connector = function () {
	var that = {};
	that.docker = {};
	that.comp_name = '';
	that.extra_options = [];

	that.add_extra_options = function (obj) {
		that.extra_options.push(obj);
	};

	that.add_extra_options_all = function (obj) {
		logger.log('info', 'Extra Options initialized: '+ JSON.stringify(obj));
		that.extra_options=obj;
	};

	that.stopAndRemove = function (container_id, endpoint, port) {
		return new Promise(async function (resolve, reject) {
			that.docker = new Docker({ //TODO:Refactor
				host: endpoint,
				port: port
			});
			that.docker.getContainer(container_id).stop(function (done) {
				that.docker.getContainer(container_id).remove(function (removed) {
					logger.log('info', 'Docker container removed! ' + container_id);
					resolve(container_id);
				});
			});
		});
	};

	that.buildAndDeploy = function (endpoint, port, port_bindings, devices, command, image, mounts, links, compo_name, host_id, environment) {
		return new Promise(async function (resolve, reject) {
			that.docker = new Docker({
				host: endpoint,
				port: port
			});

			that.docker
				.ping()
				.then(function (data) {
					logger.log("info", "Docker Host (" + host_id + ") is reachable " + endpoint);
					that.comp_name = compo_name;
					that.docker.pull(image, function (err, stream) {
						bus.emit('host-config', host_id);
						if (stream !== null) {
							stream.pipe(process.stdout, {
								end: true
							});
							stream.on('end', function () {
								that.createContainerAndStart(port_bindings, command, image, devices, mounts, links, environment).then(function (id) {
									resolve(id);
								}).catch(function (err) {
									reject(err);
								});
							});
						} else {
							that.createContainerAndStart(port_bindings, command, image, devices, mounts, links, environment).then(function (id) {
								resolve(id);
							}).catch(function (err) {
								reject(err);
							});
						}
					});
				}).catch(function (err) {
					logger.log("info", "Docker Host is not reachable " + err);
					bus.emit('container-error', host_id);
					return;
				});

		});
	}

	that.createContainerAndStart = function (port_bindings, command, image, devices, mounts, links, environment) {
		//Create a container from an image
		return new Promise(async function (resolve, reject) {
			bus.emit('container-config', that.comp_name);
			var port = '{';

			var exposedPort = '{';
			for (var i in port_bindings) {
				var item_value = port_bindings[i];
				port += '"' + item_value + '/tcp" : [{ "HostIP":"0.0.0.0", "HostPort": "' + i + '" }],';
				exposedPort += '"' + item_value + '/tcp": {},';
			}
			port = port.slice(0, -1);
			port += '}';
			exposedPort = exposedPort.slice(0, -1);
			exposedPort += '}';

			var options = {
				Image: image,
				AttachStdin: false,
				AttachStdout: true,
				AttachStderr: true,
				Tty: true,
				OpenStdin: false,
				StdinOnce: false
			};

			if ((command !== "") && (command !== undefined)) {
				if (command[0] === "-") {
					var t_c = command.split(' ');
					options.Cmd = [t_c[0], t_c[1]];
					console.log(JSON.stringify(options.Cmd));
				} else {
					options.Cmd = ['/bin/bash', '-c', command];
				}
			}

			if (environment !== undefined && environment !== "") {
				options.Env = environment;
			}

			options.HostConfig = {};
			try {
				options.ExposedPorts = JSON.parse(exposedPort);
				options.HostConfig.PortBindings = JSON.parse(port);
			} catch (e) {}

			if (links !== undefined) {
				if (links.length > 0) {
					options.HostConfig.links = links;
				}
			}

			if (devices !== undefined) {

				if (Array.isArray(devices)) {
					options.HostConfig.Devices = [];
					devices.forEach(element => {
						options.HostConfig.Devices.push({
							'PathOnHost': element.PathOnHost,
							'PathInContainer': element.PathInContainer,
							'CgroupPermissions': element.CgroupPermissions
						});
					});
				} else {
					if (devices.PathOnHost !== '') {
						options.HostConfig.Devices = [{
							'PathOnHost': devices.PathOnHost,
							'PathInContainer': devices.PathInContainer,
							'CgroupPermissions': devices.CgroupPermissions
						}];
					}
				}
			}

			//options.HostConfig.Mounts = [];
			if (mounts !== undefined && mounts !== "") {
				options.HostConfig.Binds = [];
				if (Array.isArray(mounts)) {
					mounts.forEach(element => {
						options.HostConfig.Binds.push(`${element.src}:${element.tgt}`);
					});
				} else {
					options.HostConfig.Binds.push(`${mounts.src}:${mounts.tgt}`);
				}
			}

			options.name = that.comp_name;

			if (that.extra_options.length > 0) {
				that.extra_options.forEach(element => {
					console.log(JSON.stringify(element));
					if(element[0] === "Labels"){
						options.Labels= element[1];
						console.log(JSON.stringify(options));
					}
					options.HostConfig[element[0]] = element[1];
				});
				console.log(JSON.stringify(options.HostConfig));
			}

			that.docker.createContainer(options).then(function (container) {
				container.start(function () {
					logger.log('info', 'Container started: ' + container.id + ' (' + that.comp_name + ')');
					resolve(container.id, that.comp_name);
				});

			}).catch(function (err) {
				if(err.statusCode === 409 && err.message.includes("already in use by container")){
					let id_container = err.message.split('"')[3]; //temporary hack
					logger.log('info', 'Container already started: ' + id_container + ' (' + that.comp_name + ')');
					resolve(id_container, that.comp_name);
				}else{
					bus.emit('container-error', that.comp_name);
					logger.log('info', 'Error while starting container for: ' + that.comp_name + '\n' + JSON.stringify(err));
					reject(err);
				}
			});

		});
	}



	/*
	 * Initialize Docker swarm (i.e., triggers a 'docker swarm init').
	 *
	 * The Docker API returns an HTTP Code 503 if Docker Swarm is
	 * already initialized or if the host is already part of a
	 * swarm. In this case, we do not raise an error and proceed
	 * because only need to ensure that the host is initialized
	 * (idempotency).
	 *
	 * See endpoint documentation at
	 *    https://docs.docker.com/engine/api/v1.37/#operation/SwarmInit
	 */
	that.initializeDockerSwarm = function (host) {
		return new Promise(async function (resolve, reject) {
			that.docker = new Docker({
				host: host.ip,
				port: host.port
			});
			that.docker
				.ping()
				.then(data => {
					that.docker
						.swarmInit({
							ForceNewCluster: false,
						}).then(response => {
							logger.log("info", "SWARM RESPONSE\n" + response);
							logger.log("info", "Docker Swarm initialized!");
							resolve(response.ID, component.name);
						}).catch(error => {
							if (error.message.search(/503/i) === -1) {
								logger.error("Docker Swarm error\n" + error);
								reject(error);
							}
							resolve("info", "some-id");
						});
				}).catch(error => {
					reject(error)
				});
		});
	};


	/*
	 * Start a new swarm service, from the configuration of the given component.
	 *
	 * See the documentation available at:
	 *      https://docs.docker.com/engine/api/v1.37/#operation/ServiceCreate
	 */
	that.startSwarmService = function (host, component) {
		return new Promise(async function (resolve, reject) {
			that.docker = new Docker({
				host: host.ip,
				port: host.port
			});
			that.docker
				.ping()
				.then(function (response) {
					that.docker.createService({
						"Name": component.name,
						"TaskTemplate": {
							"ContainerSpec": {
								"Image": component.docker_resource.image
							}
						}
					}).then(response => {
						logger.info(Object.keys(response));
						const identifier = response.id;
						logger.info(`Swarm service '${component.name}' started with ID '${identifier}'.`);
						resolve(identifier, component.name);

					}).catch(error => {
						logger.error(error.message);
						reject(error);

					});

				}).catch(error => {
					logger.error(error.message);
					reject(error)

				});

		});
	};


	/*
	 * Update an existing service, from the given component.
	 *
	 * Blue/Green deployments are implemented using the parameter
	 * UpdateConfig order: start-first", which forces Docker swarm to provision first new
	 * containers before it take down the older ones.
	 *
	 * See Docker endpoint documentation at:
	 *   https://docs.docker.com/engine/api/v1.37/#operation/ServiceUpdate
	 */
	that.updateSwarmService = function (host, component) {
		return new Promise(async function (resolve, reject) {
			that.docker = new Docker({
				host: host.ip,
				port: host.port
			});
			that.docker
				.ping()
				.then(function (data) {
					logger.info(`Docker host is reachable at ${host.ip}`);
					logger.info(`Updating Swarm service ${component.name} ` +
						`with ID ${component.id}!`);
					const service = that.docker.getService(component.name);
					const inspected = service
						.inspect()
						.then(data => {
							logger.info(JSON.stringify(data));
							const version = parseInt(data.Version.Index);
							logger.info(`Version = ${version}`);
							var specification = {
								"Name": component.name,
								"version": version, // The key must be lowercase!
								"TaskTemplate": {
									"ContainerSpec": {
										"Image": component.docker_resource.image
									},
									"Resources": {
										"Limits": {},
										"Reservations": {}
									},
									"RestartPolicy": {},
									"Placement": {}
								},
								"Mode": {
									"Replicated": {
										"Replicas": 1
									}
								},
								"UpdateConfig": {
									"Parallelism": 1,
									"Order": "start-first"
								},
								"EndpointSpec": {
									"ExposedPorts": [{
										"Protocol": "tcp",
										"Port": 6379
									}]
								}
							};
							service.update(
								specification,
								(error, response) => {
									if (error !== null) {
										logger.error(error);
										reject(error);
									}

									logger.info(JSON.stringify(response));
									logger.info(`Swarm service '${component.name}' updated!`);
									resolve(response.id, component.name);
								});
						}).catch(error => {
							logger.error(error.message);
							reject(error);
						});
				}).catch(error => {
					logger.error(error.message);
					reject(error);

				});
		});

	};


	return that;
};


module.exports = docker_connector;
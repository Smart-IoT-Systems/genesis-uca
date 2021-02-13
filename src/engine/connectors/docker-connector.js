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
				bus.emit('container-error', that.comp_name);
				logger.log('info', 'Error while starting container for: ' + that.comp_name + '\n' + err);
				reject(err);
			});

		});
	}


    /**
     * Create a Docker network that will connect multiple containers.
     *
     * See endpoint documentation at 
     *   https://docs.docker.com/engine/api/v1.37/#operation/NetworkCreate
     */
    that.createNetwork = async function(host, networkName) {
	await that.resetDockerHost(host);
	const specs = { "Name": networkName };
	const network = await that.docker.createNetwork(specs);
	logger.info("Docker network created (ID: '" + network.id +"')" );
	return network.id
    };


    that.resetDockerHost = async function(host) {
	that.docker = new Docker({
	    host: host.ip,
	    port: host.port
	});
	await that.docker.ping();
    }


    /*
     * Tag the given image with the given tag
     */
    that.tagImage = async function(dockerHost,  imageName, repository, tag)  {
	await that.resetDockerHost(dockerHost);
	const image = that.docker.getImage(imageName);
	await image.tag({
	    repo: repository,
	    tag: tag
	});
    }


    /*
     * Remove the given image (by ID or name)
     */
    that.removeImage = async function(dockerHost, imageName, force=false)  {
	await that.resetDockerHost(dockerHost);
	const image = that.docker.getImage(imageName);
	await image.remove({
	    force: force,
	    noprune: false
	});
    }


    /**
     * Create and start a container on the given host, according to
     * the given specifications.
     */
    that.createContainer = async function(dockerHost, containerSpecs={}, isDetached=false) {	
	const DEFAULT_SPECS = {
	    Image: 'debian:10-slim',
	    Cmd: ['/bin/bash'],
	    name: 'debian-test',
	    Tty: true
	};

	containerSpecs = Object.assign({}, DEFAULT_SPECS, containerSpecs);

	logger.info(JSON.stringify(containerSpecs));
	
	await that.resetDockerHost(dockerHost);
	try {
	    const image = that.docker.getImage(containerSpecs.Image);
	    const details = await image.inspect();
	    logger.info("Image:" + JSON.stringify(details));

	} catch (error) {
	    const pullingImage = await that.docker.createImage({fromImage: containerSpecs.Image});
	    pullingImage.pipe(process.stdout, { end: true });
	    await that.endOf(pullingImage);

	}

	const container = await that.docker.createContainer(containerSpecs);
	container.start();
	if (!isDetached) {
	    containerLog = await container.attach({stream: true, stdout: true, stderr: true});
	    containerLog.pipe(process.stdout, {end: true});
	    await that.endOf(containerLog);
	}
	
	return container.id;
    };
    

    that.endOf = function(stream) {
	return new Promise((resolve, reject) => {
	    stream.on('error', () => reject());
	    stream.on('close', () => resolve());
	    stream.on('end', () => resolve());
	    stream.on('finish', () => resolve());
	});

    }
    
    /**
     * Execute the given command on the container with the given ID.
     *
     * See the documentation at
     * https://docs.docker.com/engine/api/v1.37/#tag/Exec
     */
    that.executeCommand = async function(dockerHost, containerID, commandSpecs={}) {
	const DEFAULT_SPECS = {
	    Cmd: ["/bin/bash", "-c",  "ls  -l"],
	    AttachStdout: true,
	    AttachStderr: true,
	    Tty: true
	};

	commandSpecs = Object.assign({}, DEFAULT_SPECS, commandSpecs);

	logger.info(`Executing '${JSON.stringify(commandSpecs)} ...`);
	
	await that.resetDockerHost(dockerHost);
	const container = that.docker.getContainer(containerID);
	const execution = await container.exec(commandSpecs);
	const stream = await execution.start();
	stream.output.pipe(process.stdout, {end: true});
	await that.endOf(stream.output);
    };


    /**
     * Save a container into a new image
     *
     * See documentation at:
     *    https://docs.docker.com/engine/api/v1.37/#operation/ImageCommit
     */
    that.saveContainerAsImage = async function(dockerHost, containerID, imageName) {
	await that.resetDockerHost(dockerHost);

	const container = that.docker.getContainer(containerID);
	const commitSpecs = {
	    repo: imageName,
	    comment: 'GeneSIS build'
	};
	const commit = await container.commit(commitSpecs);
	return commit.Id;
    }

    
    /* 
     * Connect a container to an existing network. 
     *
     * The container is identified by its ID while the network is
     * identified by its name.
     */
    that.connectContainerToNetwork = async function(dockerHost, networkName, containerID) {
	await that.resetDockerHost(dockerHost);
	const network = that.docker.getNetwork(networkName);
	const inspected = await network.inspect()
	const containerSpecs = {
	    "Container": containerID,
	    "EndpointConfig": null
	};
	await network.connect(containerSpecs);
    };


    /*
     * Stop a running container
     */
    that.stopContainer = async function(dockerHost, containerID) {
	await that.resetDockerHost(dockerHost);
	await that.docker.ping();
	const container = that.docker.getContainer(containerID);
	await container.stop();
    }


    /**
     * Destroy a given container, that stop and remove it.
     *
     * See API documentation available at
     * https://docs.docker.com/engine/api/v1.37/#operation/ContainerArchive
     *
     */
    that.removeContainer = async function(dockerHost, containerID) {
	await that.resetDockerHost(dockerHost);
	await that.docker.ping();
	const container = that.docker.getContainer(containerID);
	await container.remove();
	return containerID;
    }


    /**
     * Upload an archive on a given container.
     *
     * See API documentation available at
     * https://docs.docker.com/engine/api/v1.37/#operation/ContainerArchive
     *
     * @param container the ID (String) of the container where the
     * archive is to be uploaded.
     *
     * @param archive (String) the path to the archive to upload
     *
     * @param path (String) the path where the archive should be
     * placed on the target container.
     */
    that.uploadArchive = async function(dockerHost, containerID, archive, path) {
	await that.resetDockerHost(dockerHost);
	logger.info(`Uploading archive on container ${containerID}`);
	const container = that.docker.getContainer(containerID);
	const response = await container.putArchive(archive, { path: path });
	logger.info(`Uploading '${archive}' to' ${containerID}'`);
	logger.info(`   ${response.statusCode}: ${response.statusMessage}`);
    };
    


    /**
     * Initialize Docker swarm (i.e., triggers a 'docker swarm
     * init') on the given host.
     *
     * The Docker API returns an HTTP Code 503 if Docker Swarm is
     * already initialized or if the host is already part of a
     * swarm. In this case, we do not raise an error and proceed
     * because we only need to ensure that the host is initialized
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

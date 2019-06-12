var Docker = require('dockerode');
var bus = require('../event-bus.js');
var logger = require('../logger.js');

var docker_connector = function () {
    var that = {};
    that.docker = {};
    that.comp_name = '';

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

    that.buildAndDeploy = function (endpoint, port, port_bindings, devices, command, image, mounts, links, compo_name, host_id) {
        return new Promise(async function (resolve, reject) {
            that.docker = new Docker({
                host: endpoint,
                port: port
            });

            that.docker.ping().then(function(data){
                logger.log("info", "Docker Host ("+host_id+") is reachable "+endpoint);
                that.comp_name = compo_name;
                that.docker.pull(image, function (err, stream) {
                    bus.emit('host-config', host_id);
                    if (stream !== null) {
                        stream.pipe(process.stdout, {
                            end: true
                        });
                        stream.on('end', function () {
                            that.createContainerAndStart(port_bindings, command, image, devices, mounts, links).then(function (id) {
                                resolve(id);
                            }).catch(function (err) {
                                reject(err);
                            });
                        });
                    } else {
                        that.createContainerAndStart(port_bindings, command, image, devices, mounts, links).then(function (id) {
                            resolve(id);
                        }).catch(function (err) {
                            reject(err);
                        });
                    }
                });
            }).catch(function(err){
                logger.log("info", "Docker Host is not reachable "+err);
                bus.emit('container-error', host_id);
                return;
            });

        });
    }

    that.createContainerAndStart = function (port_bindings, command, image, devices, mounts, links) {
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

            if (command !== "") {
                if(command[0] === "-"){
                    var t_c=command.split(' ');
                    options.Cmd = [t_c[0], t_c[1]];
                    console.log(JSON.stringify(options.Cmd));
                }else{
                    options.Cmd = ['/bin/bash', '-c', command];
                }
            }

            options.HostConfig = {};
            try {
                options.ExposedPorts = JSON.parse(exposedPort);
                options.HostConfig.PortBindings = JSON.parse(port);
            } catch (e) {}

            if(links !== undefined){
                if(links.length > 0){
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

            options.HostConfig.Mounts = [];
            if (mounts !== undefined) {			
				options.HostConfig.Mounts.push({
					"Source": mounts.src,
					"Target": mounts.tgt,
					"ReadOnly": false,
					"Type": mounts.type
				});
            }
			console.log(options.HostConfig.Mounts);

            options.name = that.comp_name;

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



    return that;
};

module.exports = docker_connector;
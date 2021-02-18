var comparison_engine = require('./model-comparison.js');
var dockerConnector = require('./connectors/docker-connector.js');
var fs = require('fs');
var lodash = require('lodash');
var logger = require('./logger.js');
var sshConnection = require('./connectors/ssh-connector.js');
var tar = require("tar");
var temp = require("temp");
var utils = require("../util.js");




/*
 * Maintains a mapping between components and their "availability"
 * agent. This agent varies according to the strategy (i.e., built-in
 * or Docker Swarm).
 */
class AvailabilityManager {

    constructor () {
	this._agents = new Map();
    }


    /*
     * Manage the availability of a given component. 
     *
     * Install from scratch when given a new component, or adjust
     * configuration of already managed components. If the strategy
     * has changed, we uninstall the component and select a new agent
     * that will reinstall according to the new strategy.
     */
    async handle(givenComponent, givenHost) {
	if (this.knowsAbout(givenComponent)) {
	    const agent = this._agents.get(givenComponent.name);
	    if (agent.canHandle(givenComponent)) {
		await agent.update(givenComponent);

	    } else {
		logger.info(`Availability strategy for '${givenComponent.name}' changed to ${givenComponent.availability.strategy}`);
		await agent.uninstall();
		const newAgent = this._selectAgentFor(givenComponent, givenHost);
		await newAgent.installFromScratch();

	    }
	    
	} else {
	    const agent = this._selectAgentFor(givenComponent, givenHost);
	    await agent.installFromScratch();

	}
    }

    
    knowsAbout(givenComponent) {
	return this._agents.has(givenComponent.name);
    }
    

    /*
     * Create the right availability agent depending on the
     * availability strategy that the user has chosen. If the
     * component lacks a DockerResource, we wrap it into a SSH
     * Adapter, which builds the Docker image from the SSH resource.
     */
    _selectAgentFor(givenComponent, givenHost) {
	var agent = null;
	if ( givenComponent.availability.usesDockerSwarm() ) {
	    agent = new DockerSwarmAgent(givenComponent, givenHost);

	} else if ( givenComponent.availability.isBuiltin() ) {
	    agent = new BuiltinAgent(givenComponent, givenHost);

	} else {
	    throw new Error(`Unknown availability policy '${givenComponent.availability}'`);

	}

	if (!givenComponent.hasDockerResource())  {
	    if (givenComponent.hasSSHResource()) {
		agent = new SSHAdapter(agent);

	    } else  {
		const message = `Availability requires either Docker or SSH resources`;
		throw new Error(message);

	    }
	}
	
	this._agents.set(givenComponent.name, agent);
	logger.info(`Availability mechanisms activated for ${givenComponent.name}`)
	return agent;
    }

}



class AvailabilityAgent {

    constructor(givenComponent, givenHost)  {
	this._component = givenComponent;
	this._host = givenHost;
	this._docker = dockerConnector();
	this._forceComponentUpdate = false;
    }

    /*
     * Update the components according to both the new availability
     * policy and the new component configuration.
     *
     * We first update the availability policy (in memory), and then
     * trigger possible adjustment of the running components. Should
     * both have been updated, the policy should be handled first to
     * be in place when we update the configuration.
     */
    async update(newConfiguration) {
	this._forceComponentUpdate = false;
	this._updateAvailabilityPolicy(newConfiguration);
	this._updateConfiguration(newConfiguration);
    }

    
    async _updateAvailabilityPolicy(givenComponent) {
	const changes = this._comparePolicies(this._component.availability,
					      givenComponent.availability);
	for(const anyChange of changes) {
	    switch (anyChange.property) {

	    case "strategy":
		//  Ignored. This is already handled in the method
		//  AvailabilityManager::handle(component)
		break;

	    case "replicaCount":
		await this._updateReplicaCount(anyChange.newValue);
		break;

	    case "healthCheck":
		await this._updateHealthCheckScript(anyChange.newValue);
		break;

	    case "zeroDownTime":
		await this._updateZeroDownTime(anyChange.newValue);
		break;

	    default:
		const message =
		    `Property '${anyChange}' is not yet supported in availability policies!`;
		throw new Error(message);

	    }
	}
    }

    _comparePolicies(oldPolicy, newPolicy) {
	var changes = [];
	if (oldPolicy.strategy !== newPolicy.strategy) {
	   changes.push({property: "strategy",
			 newValue: newPolicy.strategy});
	}
	if (oldPolicy.replicaCount !== newPolicy.replicaCount) {
	    changes.push({property: "replicaCount",
			  newValue: newPolicy.replicaCount});
	}
	if (oldPolicy.healthCheck !== newPolicy.healthCheck) {
	    changes.push({property: "healthCheck",
			  newValue: newPolicy.healthCheck});
	}
	if (oldPolicy.zeroDownTime != newPolicy.zeroDownTime) {
	    changes.push({property: "zeroDownTime",
			  newValue: newPolicy.zeroDownTime});
	}
	return changes;
    }


    async _updateConfiguration(givenComponent) {
	const changes = this._compareConfigurations(this._component, givenComponent);
	if (this.forceComponentUpdate || changes.length > 0) {
	    this._info(`Some changes requires a redeployment of the ${this._component.name}`);
	    await this._updateComponent(givenComponent);
	}
    }

    _compareConfigurations(oldComponent, newComponent) {

	const uselessProperties = ["add_property",
				   "remove_property",
				   "get_all_properties",
				   "availability"];
	
	var changes = []
	for (const eachProperty in newComponent) {
	    if (uselessProperties.includes(eachProperty)) continue;

	    if (eachProperty in oldComponent) {
		if (!lodash.isEqual(oldComponent[eachProperty], newComponent[eachProperty])) {
		    changes.push(eachProperty);
		}
		
	    } else {
		changes.push(eachProperty);
		
	    }
	}

	for (const anyProperty in oldComponent) {
	    if (changes.includes(anyProperty)) continue;
	    if (uselessProperties.includes(anyProperty)) continue;
	    if ( !(anyProperty in newComponent) ){
		changes.push(anyProperty);
	    }
	}
	return changes;
    }


    _info(message) {
	logger.info("AVAILABILITY: " + message);
    }

    
    _error(message) {
	logger.error("AVAILABILITY: " + message);
    }

}



class BuiltinAgent extends AvailabilityAgent {

    constructor (givenComponent, givenHost) {
	super(givenComponent, givenHost);

	// Hold the runtime information about the builtin mechanisms
	// that are deployed
	this._runtime = {
	    networkID: null,
	    lastReplicaID: 0,
	    activeReplicas: [],
	    replicasToStop: [],
	    proxyID: null
	};
    }

    
    canHandle(givenComponent) {
	return givenComponent.availability.strategy === "Builtin";
    }


    async installFromScratch() {
	await this.ensureHostIsReady();
	await this._deploy();
    }


    async ensureHostIsReady () {
	const hostIsReady = await this._docker.isReady(this._host);
	if (!hostIsReady) {
	    await this._installDockerInRemoteMode();
	}
    }
    
    
    async _installDockerInRemoteMode() {
	const ssh = sshConnection(this._host.ip,
				  this._host.port,
				  this._component.ssh_resource.credentials.username,
				  this._component.ssh_resource.credentials.password,
				  this._component.ssh_resource.credentials.sshkey,
				  this._component.ssh_resource.credentials.agent);
	try {
	    const DOCKER_INSTALLATION_SCRIPT = "install_docker_in_remote_mode.sh";
	    await ssh.upload_file(DOCKER_INSTALLATION_SCRIPT,
				  DOCKER_INSTALLATION_SCRIPT);
	    await ssh.execute_command(`source ${DOCKER_INSTALLATION_SCRIPT}`);
	    this._host.port = "2376";
	    this._info(`Remote Docker installed on host ${this._host.ip}.`);
	    
	} catch (error) {
	    utils.chainError("Unable to install Docker in remote mode.", error);
	    
	}
    }

    async _deploy() {
	try {
	    await this._setupDockerNetwork();
	    await this._createManyReplicas(this._component.availability.replicaCount);
	    await this._deployBuiltinProxy();
	    await this._reconfigureProxy();
	    this._info("Builtin deployment complete!");
	    
	} catch (error) {
	    utils.chainError("Unable to deploy builtin availability mechanisms!",
		       error);
	    
	}
    }


    async _setupDockerNetwork () {
	this._runtime.networkID = `GeneSIS-${this._component.name}`;
	try{
	    const networkSpecs = {
		Name: this._runtime.networkID
	    };
	    await this._docker.createNetwork(this._host, networkSpecs);
	    this._info(`Network ${this._runtime.networkID} created!`);
	    
	} catch (error) {
	    utils.chainError("Unable to create a dedicated Docker network!",
		       error);
	    
	}
    }

    async _createManyReplicas(replicaCount) {
	try {
	    for (var i=0 ; i<replicaCount ; i++)  {
		const name = this._generateReplicaName();
		await this._createOneReplica(name);
	    }
	    
	} catch (error) {
	    utils.chainError(`Unable to replicate '${this._component.name}'.`, error);
	    
	}
    }

    
    _generateReplicaName() {
	this._runtime.lastReplicaID++;
	return `${this._component.name}-${this._runtime.lastReplicaID}`;
    }

    
    async _createOneReplica(replicaName) {
	const containerSpecs = {
	    "Image": this._component.docker_resource.image,
	    "name": replicaName, // /!\ Must be capitalized 'name'
	    "Cmd": [ "/bin/bash", "-c",  this._component.ssh_resource.startCommand ]
	};
	await this._docker.createContainer(this._host, containerSpecs, true);
	await this._docker.connectContainerToNetwork(this._host,
						     this._runtime.networkID,
						     replicaName);
	this._runtime.activeReplicas.push(replicaName);
	this._info(`${replicaName} of ${this._component.name} created!`);	
    }


    async _deployBuiltinProxy() {
	try {
	    const exposedPort = this._component.availability.exposedPort;
	    const proxySpecs = {
		Image: "fchauvel/enact-nginx:latest",
		name: `${this._component.name}-proxy`,
		Cmd: ["/bin/bash", "-c", "nginx -g 'daemon off;'"],
		Tty: false,
		AttachStdin: false,
		AttachStdout: false,
		AttachStderr: false,
		HostConfig: {
		    PortBindings: {
			[`${exposedPort}/tcp`]: [{ HostPort: `${exposedPort}` }]
		    },
		},
		ExposedPorts: {
		    [`${exposedPort}/tcp`]: {}
		},
	    };
	    await this._docker.createContainer(this._host, proxySpecs, true);
	    await this._docker.connectContainerToNetwork(this._host,
							 this._runtime.networkID,
							 proxySpecs.name);
	    this._runtime.proxyID = proxySpecs.name;

	    await this._configureRemoteDockerAPI();

	    this._info("Proxy created!");

	} catch (error) {
	    utils.chainError("Unable to deploy the proxy!", error);

	}
    }


    async _configureRemoteDockerAPI() {
	try {
	    const dockerAPI = `${this._host.ip}:${this._host.port}`; 
	    const imageName = this._component.docker_resource.image;
	    const command = this._component.docker_resource.cmd;
	    const networkID = this._runtime.networkID;
	    const commandSpecs = {
		Cmd:  ["/bin/bash",
		       "-c",
		       `bash ./endpoints.sh configure-docker ${dockerAPI} ${imageName} '${command}' ${networkID} `],
	    }
	    await this._docker.executeCommand(this._host, this._runtime.proxyID, commandSpecs);
	    this._info(`Remote Docker API set on the proxy`);
	    
	} catch (error) {
	    utils.chainError("Unable to configure the remote Docker API on the proxy!", error);
	    
	}

    }

    
    async _reconfigureProxy() {
	try {
	    await this._uploadHealthcheckScript(this._component.availability.healthCheck);
	    await this._registerAllReplicas();
	    await this._restartProxy();
	    this._info("Proxy configured!");

	} catch (error) {
	    utils.chainError("Unable to reconfigure NGinx!", error);

	}
    }

    
    async _uploadHealthcheckScript(script) {
	try {
	    const archive = await this._archiveScript(script);
	    await this._docker.uploadArchive(this._host,
					     this._runtime.proxyID, archive, "/enact");
	    this._info("Heatlh check script uploaded");
	    
	} catch (error) {
	    utils.chainError("Unable to upload the health script!", error);
	    
	}
    }


    /*
     * Create a TAR archive containing the healthcheck script provided
     * by the user.
     */
    async _archiveScript(healthcheck, options={}) {
	const OPTION_DEFAULTS = {
	    scriptName: "healthcheck.sh",
	    archiveName: "healthcheck.tar.gz",
	    prefix: "genesis"
	};
	options = Object.assign({}, OPTION_DEFAULTS, options);
	
	try {	    
	    const path = await temp.mkdir(options.prefix);
	    
	    // Dump the healthcheck code into a file
	    const script = path + "/" + options.scriptName;
	    fs.writeFileSync(script, healthcheck);
	    
	    // Create a tarball including the script file
 	    const archivePath = `${path}/${options.archiveName}`;
	    await tar.c({   gzip: true,
			    file: archivePath,
			    cwd: path
			},
			[options.scriptName]);
	    
	    this._info(`Archive ready at '${path}/${options.archiveName}'`);
	    return archivePath;

	} catch (error) {
	    utils.chainError("Unable to write the health check script on disk!", error);

	}
    }


    async _registerAllReplicas() {
	try {
	    for (const eachEndpoint of this._runtime.activeReplicas) {
		const commandSpecs = {
		    Cmd:  [
			"/bin/bash",
			"-c",
			`bash ./endpoints.sh register ${this._urlOf(eachEndpoint)}`
		    ],
		}
		await this._docker.executeCommand(this._host,
						  this._runtime.proxyID,
						  commandSpecs);
		this._info(`Endpoint ${eachEndpoint} registered to the proxy!`);
	    }
	    
	} catch (error) {
	    utils.chainError("Unable to register endpoints to the proxy!",
			     error);
	    
	}
    }

    
    _urlOf(endpoint) {
	// /!\ NGinx 1.19.6 (at least) throws "Invalid host in
	// upstream" if the endpoint URL starts with 'http://'
	return `${endpoint}:${this._component.availability.exposedPort}`;
    }

    
    async _restartProxy() {
	const activeEndpoint = this._runtime.activeReplicas[0];
	const exposedPort = this._component.availability.exposedPort;
	try {
	    const commandSpecs = {
		Cmd:  ["/bin/bash",
		       "-c",
		       `bash ./endpoints.sh initialize ${exposedPort} ${this._urlOf(activeEndpoint)}`],
	    }
	    await this._docker.executeCommand(this._host, this._runtime.proxyID, commandSpecs);
	    this._info(`Endpoint ${activeEndpoint} activated!`);
	    
	} catch (error) {
	    utils.chainError("Unable to initialize the proxy!", error);
	    
	}

    }

    
    async _updateReplicaCount(newCount) {
	this._component.availability.replicaCount = newCount;
	this._forceComponentUpdate = true;
    }

    
    async _updateHealthCheckScript(newScript) {
	this._uploadHealthcheckScript(newScript);
    }

    
    async _updateZeroDownTime(newValue) {
	this._component.availability.zeroDownTime = newValue;
    }

    
    async _updateComponent(givenComponent) {
	const policy = this._component.availability;
	try {
	    if (policy.zeroDownTime) {
		this._markAllReplicasForTermination();
		await this._createManyReplicas(policy.replicaCount);
		await this._stopMarkedReplicas();
		
	    } else {
		this._markAllReplicasForTermination();
		await this._stopMarkedReplicas();
		await this._createReplicas(policy.replicaCount);
		await this._restartProxy();

	    }
	    this._info(`All ${this._component.name} replica(s) updated!`);
	    
	} catch (error) {
	    utils.chainError(`Unable to update all '${this._component.name}' replicas `, error);
	    
	}
	    	
    }



    
    _markAllReplicasForTermination() {
	this._runtime.replicasToStop = [];
	for(const eachReplica of this._runtime.activeReplicas) {
	    this._runtime.replicasToStop.push(eachReplica);
	}
    }


    async _stopMarkedReplicas() {
	try {
	    const replicaCount = this._runtime.replicasToStop.length;
	    while (this._runtime.replicasToStop.length > 0) {
		const eachMarkedReplica = this._runtime.replicasToStop.pop();
		await this._stopReplica(eachMarkedReplica);	    
	    }
	    this._info(`${replicaCount} replica(s) of ${this._component.name} stopped.`);

	} catch (error) {
	    utils.chainError(
		`Could not stop all selected replicas of ${this._component.name}`,
		error
	    );
	    
	}
	
    }


    async _stopReplica(markedReplica) {
	try {
	    await this._deregisterReplica(markedReplica);
	    await this._docker.stopContainer(this._host, markedReplica);
	    await this._docker.removeContainer(this._host, markedReplica);
	    
	} catch (error) {
	    utils.chainError(`Unable to terminate replica ${markedReplica}.`, error);
	    
	}
    }


    /*
     * Deregister a given replica/endpoint from the proxy.
     *
     * This triggers the endpoints.sh on the proxy container. In turn
     * it removes the given endpoint from the list of endpoints and
     * terminate the associated watchdog (i.e., a CRON task).
     */
    async _deregisterReplica (givenReplica) {
	try {
	    const commandSpecs = {
		Cmd:  [
		    "/bin/bash",
		    "-c",
		    `bash ./endpoints.sh discard ${this._urlOf(givenReplica)}`
		],
	    }
	    await this._docker.executeCommand(this._host,
					      this._runtime.proxyID,
					      commandSpecs);
	    this._info(`Replica ${givenReplica} deregistered from the proxy!`);
		    

	} catch (error) {
	    utils.chainError(`Unable to deregister '${givenReplica}'.`, error);
	    
	}

    }

    
    async uninstall (givenComponent) {
	this._markAllReplicasForTermination();
	await this._stopMarkedReplicas();
	await this._stopProxy();
    }


    async _stopProxy() {
	try {
	    const containerID = this._runtime.proxyID;
	    await this._docker.stopContainer(this._host, containerID);
	    await this._docker.removeContainer(this._host, containerID);

	} catch (error) {
	    utils.chainError('Unable to terminate the proxy!', error);
	    
	}

    }

        
}



class DockerSwarmAgent extends AvailabilityAgent {

    constructor (givenComponent) {
	super(givenComponent);
    }

    canHandle(givenComponent) {
	return givenComponent.availability.strategy === "Docker Swarm";
    }

    async installFromScratch() {
	await this._docker.initializeDockerSwarm(this._host); // Idempotent
	await this._docker.startSwarmService(this._host, this._component);
    }

    async _updateReplicaCount(newCount) {
	this._component.availability.replicaCount = newCount;
    }

    async _updateHealthCheckScript(newScript) {
	const message = "Health check script are not supported by DockerSwarm";
	throw new Error(message);
    }

    async _updateZeroDownTime(newValue) {
	this._info(`Updating the zeroDownTime to ${newValue}`);
    }

    async _updateComponent(givenComponent) {
	return await this._docker.updateSwarmService(this._host, this._component);
    }

    async uninstall(givenComponent) {
	this._error("Uninstallation is not yet supported!");
    }

}


/* 
 * This is an adapter class, which ensure that the SSH resources
 * associated with the target component first converted into a
 * DockerResources so that the installation can proceed.
 */
class SSHAdapter extends AvailabilityAgent {

    constructor (delegate) {
	super(null);
	this._delegate = delegate;
    }


    canHandle(givenComponent) {
	return this._delegate.canHandle(givenComponent)
	    && givenComponent.givenComponent.hasSSHResource();   
    }

    
    async installFromScratch() {
	await this._delegate.ensureHostIsReady();
	await this._createDockerImageFromSSHResources();	
	await this._delegate.installFromScratch();
    }
    
    
    async _createDockerImageFromSSHResources() {
	const baseImage = "debian:10-slim";
	const containerName = "enact-tmp";
	const imageName = this._dockerImageName("latest");
	try {
	    const installationScript = [
		"/bin/bash",
		"-c",
		this._sshResource.downloadCommand
		    + "; " + this._sshResource.installCommand
		    + "; " + this._sshResource.configureCommand
	    ];
	    
	    const containerID =
		  await this.docker.createContainer(this.host,
						     {
							 Image: baseImage,
							 Cmd: installationScript,
							 name: containerName
						     });
	    await this.docker.saveContainerAsImage(this.host,
						    containerID,
						    imageName);
	    await this.docker.removeContainer(this.host, containerName);
	    this._dockerResource.image = imageName;
	    this._dockerResource.cmd = this._sshResource.startCommand;
	    this._info(`New docker image '${imageName}' for component '${this.component.name}'.`);

	} catch (error) {
	    utils.chainError(
		`Unable to build a Docker image from the SSH resources of '${this.component.name}'.`,
		error
	    );

	}    
	
    }

    
    get docker() {
	return this._delegate._docker;
    }

    
    get host() {
	return this._delegate._host;
    }

    _dockerImageName(tag) {
	if (tag === undefined) {
	    return `${this.component.name}-livebuilt`;
	}   
	return `${this.component.name}-livebuilt:${tag}`;
    }

    
    get _sshResource()  {
	return this._delegate._component.ssh_resource;
    }

    
    get _dockerResource() {
	return this._delegate._component.docker_resource;
    }


    get component() {
	return this._delegate._component;
    }


    async _updateReplicaCount(newCount) {
	this._delegate._updateReplicaCount(newCount);
    }

    
    async _updateHealthCheckScript(newScript) {
	this._delegate._updateHealthCheckScript(newScript);
    }

    
    async _updateZeroDownTime(newValue) {
	this._delegate._updateZeroDownTime(newValue);
    }

    
    async _updateComponent(givenComponent) {
	try {
	    await this._tagImageAsOld();
	    await this._createDockerImageFromSSHResources();
	    await this._delegate.updateComponent(givenComponent);
	    await this._deleteDockerImage("old");

	} catch (error) {
	    utils.chainError(`Unable to update all '${this.component.name}' replicas `, error);
	    
	}
    }


    async _tagImageAsOld() {
	const currentTag = this._dockerImageName("latest");
	const repository = this._dockerImageName(); // /!\ without the tag!
	try {
	    await this.docker.tagImage(this.host, currentTag, repository, "old");

	} catch (error) {
	    utils.chainError(`Could not tag image '${currentTag}' as '${repository}:old'.`, error);
	    
	}
    }


    async _deleteDockerImage(tag) {
	const imageName = this._dockerImageName(tag);
	try {
	    const forceRemoval = true;
	    await this.docker.removeImage(this.host, imageName, forceRemoval);

	} catch (error)  {
	    utils.chainError(`Could not delete Docker image '${imageName}'`, error);
	    
	}
	
    }

    
    async uninstall(givenComponent) {
	await this._delegate.uninstall(givenComponent);
	await this._deleteDockerImage("latest");
    }
    
}


module.exports = AvailabilityManager;


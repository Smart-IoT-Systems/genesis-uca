/*****************************/
/*Deployment model           */
/*Contain all nodes and links*/
/*****************************/

var uuidv4 = require('uuid/v4');

var deployment_model = function (spec) {
    var that = {};
    that.name = spec.name || 'a _deployment_name';
    that.components = [];
    that.links = [];
    that.containments = [];
    that.type_registry = [];

    that.node_factory = function () {
        var component;

        this.create_component = function (type, spec) {
            if (type === "/infra/device") {
                component = device(spec);
            } else if (type === "/infra/vm_host") {
                component = vm_host(spec);
            } else if (type === "/infra/docker_host") {
                component = docker_host(spec);
            } else if (type === "/external") {
                component = external_node(spec);
            } else if (type === "/internal/node_red") {
                component = node_red(spec);
            } else if (type === "/internal") {
                component = software_node(spec);
            } else {
                for (var i = 0; i < that.type_registry.length; i++) {
                    if (type.indexOf(that.type_registry[i].id.split('.')[0]) >= 0) { //To be updated
                        component = that.type_registry[i].module(spec);
                        return component;
                    }
                }
            }

            return component;
        }
        return this;
    };

    
    that.add_component = function (component) {
        that.components.push(component);
    };

    that.add_link = function (link) {
        // A node can only be controlled by one controller
        // One node can control several other nodes
        if (link.isControl) {
            if ((that.get_all_inputs_of_component(link.target).length > 0) || (that.get_all_outputs_of_component(link.target).length > 0)) { //no inputs no outputs
                console.log("Cannot create this link, target node already has links in or out");
            } else {
                that.links.push(link);
            }
        } else {
            that.links.push(link);
        }

    };

    that.add_containment = function (containment) {
        //A node can only be contained once.
        for (var i in that.containments) {
            if (that.containments[i].target === containment.target) {
                console.log("Cannot create this containment, target node is already contained");
                return;
            }
        }

        that.containments.push(containment);
    }


    that.remove_component = function (component) {
        var i = that.components.indexOf(component);
        if (i > -1) {
            that.components.splice(i, 1); //The second parameter of splice is the number of elements to remove. Note that splice modifies the array in place and returns a new array containing the elements that have been removed. 
        }
        //we also need to remove the associated links
        var tab_indexes = [];
        for (var i in that.links) {
            var tmp_l_name_target = that.get_comp_name_from_port_id(that.links[i].target);
            var tmp_l_name_src = that.get_comp_name_from_port_id(that.links[i].src);
            if (component.name !== tmp_l_name_target &&
                component.name !== tmp_l_name_src) {
                tab_indexes.push(that.links[i]);
            }
        }
        if (tab_indexes.length > 0) {
            /*console.log(JSON.stringify(tab_indexes));
            tab_indexes.forEach(function (elem) {
                that.links.splice(elem, 1);
            });*/
            that.links = tab_indexes;
        }
        //we also need to remove the associated containments
        var tabc_indexes_cont = [];
        for (var i in that.containments) {
            var tmp_c_name_target = that.get_comp_name_from_port_id(that.containments[i].target);
            var tmp_c_name_src = that.get_comp_name_from_port_id(that.containments[i].src);
            if (component.name === tmp_c_name_target ||
                component.name === tmp_c_name_src) {
                tabc_indexes_cont.push(i);
            }
        }
        if (tabc_indexes_cont.length > 0) {
            tabc_indexes_cont.forEach(function (elem) {
                that.containments.splice(elem, 1);
            });
        }

    };

    that.remove_link = function (link) {
        var i = that.links.indexOf(link);
        if (i > -1) {
            that.links.splice(i, 1);
        }
    };

    that.remove_containment = function (containment) {
        var i = that.containments.indexOf(containment);
        if (i > -1) {
            that.containments.splice(i, 1);
        }
    };

    that.revive_components = function (comps) {
        that.components = [];
        for (var i in comps) {
            var f = that.node_factory();
            var node = f.create_component(comps[i]._type, comps[i]);
            that.components.push(node);
        }
    };

    that.revive_links = function (links) {
        that.links = [];
        for (var i in links) {
            var l = link(links[i]);
            that.links.push(l);
        }
    };

    that.revive_containments = function (containments) {
        that.containments = [];
        for (var i in containments) {
            var l = hosting(containments[i]);
            that.containments.push(l);
        }
    };

    that.find_node_named = function (name) {
        var tab = that.components.filter(function (elem) {
            if (elem.name === name) {
                return elem;
            }
        });
        if (tab.length > 0) {
            return tab[0];
        }
    };

    that.find_containment_named = function (name) {
        var tab = that.containments.filter(function (elem) {
            if (elem.name === name) {
                return elem;
            }
        });
        if (tab.length > 0) {
            return tab[0];
        }
    };

    that.find_link_named = function (name) {
        var tab = that.links.filter(function (elem) {
            if (elem.name === name) {
                return elem;
            }
        });
        if (tab.length > 0) {
            return tab[0];
        }
    };

    that.get_hosted = function (name) {
        var tab = that.containments.filter(function (elem) {
            if (that.get_comp_name_from_port_id(elem.target) === name) {
                return elem;
            }
        });
        return tab;
    };

    that.get_all_hosted = function () {
        var tab = [];
        that.containments.forEach(function (elem) {
            if (elem.target !== null) {
                var comp_name = that.get_comp_name_from_port_id(elem.target);
                tab.push(that.find_node_named(comp_name));
            }
        });
        return tab;
    };

    that.get_all_can_be_hosted = function () {
        var tab = that.components.filter(function (elem) {
            if (elem._type.split('/')[1].indexOf("internal") > -1) {
                return elem;
            }
        });
        return tab;
    }

    that.get_all_software_components = function () {
        var tab = that.components.filter(function (elem) {
            if ((elem._type.indexOf('internal') >= 0) || (elem._type.indexOf('external') >= 0)) {
                return elem;
            }
        });
        return tab;
    }

    that.get_all_internals = function () {
        var tab = that.components.filter(function (elem) {
            if (elem._type.indexOf('internal') >= 0) {
                return elem;
            }
        });
        return tab;
    };

    that.get_all_deployer_links = function () {
        var tab = that.links.filter(function (elem) {
            if (elem.isDeployer) {
                return elem;
            }
        });
        return tab;
    }

    that.get_all_inputs_of_component = function (comp) {
        var tab = that.links.filter(function (elem) {
            if (that.get_comp_name_from_port_id(elem.target) === comp.name) {
                for (var e of comp.required_communication_port) {
                    if (that.get_port_name_from_port_id(elem.target) === e.name) {
                        return elem;
                    }
                }
            }
        });
        return tab;
    };

    that.get_all_outputs_of_component = function (comp) {
        var tab = that.links.filter(function (elem) {
            if (that.get_comp_name_from_port_id(elem.src) === comp.name) {
                for (var e of comp.provided_communication_port) {
                    if (that.get_port_name_from_port_id(elem.src) === e.name) {
                        return elem;
                    }
                }
            }
        });
        return tab;
    };

    that.need_deployment_agent = function (comp) {
        that.get_all_inputs_of_component(comp).forEach(function (elem) {
            if (elem.isDeployer) {
                return true;
            }
        });
        return false;
    };

    that.find_link_of_required_port = function (id) {
        var result = null;
        that.links.forEach(function (elem) {
            if (elem.target === id) {
                result = elem;
            }
        });
        return result;
    }

    that.find_containment_of_required_port = function (id) {
        var result = null;
        that.containments.forEach(function (elem) {
            if (elem.target === id) {
                result = elem;
            }
        });
        return result;
    }

    that.find_link_of_provided_port = function (id) {
        var result = null
        that.links.forEach(function (elem) {
            if (elem.src === id) {
                result = elem;
            }
        });
        return result;
    }

    that.find_containment_of_provided_port = function (id) {
        var result = null
        that.containments.forEach(function (elem) {
            if (elem.src === id) {
                result = elem;
            }
        });
        return result;
    }

    that.is_top_component = function (elem) {
        var result = true;
        for (var e of elem.provided_execution_port) {
            var id_p = that.generate_port_id(elem, e);
            var t = that.find_containment_of_provided_port(id_p);
            if (t !== null) {
                result = false;
            }
        };
        return result;
    }

    that.generate_port_id = function (comp, port) {
        return '/' + comp.name + '/' + port.name;
    }

    that.get_comp_name_from_port_id = function (id) {
        return id.split('/')[1];
    };

    that.get_port_name_from_port_id = function (id) {
        return id.split('/')[2];
    };

    that.find_host = function (comp) {
        var h = that.find_host_one_level_down(comp);
        if (h === null) {
            return null;
        } else {
            if (h._type.indexOf('internal') >= 0) {
                return that.find_host(h);
            } else {
                return h;
            }
        }
    };

    that.find_host_one_level_down = function (comp) {
        var id = that.generate_port_id(comp, comp.required_execution_port);
        var containment = that.find_containment_of_required_port(id);
        if (containment) {
            var port_host = containment.src;
            var host_name = that.get_comp_name_from_port_id(port_host);
            return that.find_node_named(host_name);
        } else {
            return null;
        }
    };

    that.get_all_mandatory_of_a_component = function (comp) {
        var all_mandatories = [];
        comp.required_communication_port.forEach(function (elem) {
            if (elem.isMandatory) {
                var id = that.generate_port_id(comp, elem);
                var link = that.find_link_of_required_port(id);
                if (link) {
                    var port_other = link.src;
                    var other = that.get_comp_name_from_port_id(port_other);
                    all_mandatories.push(that.find_node_named(other));
                }
            }
        });

        return all_mandatories;
    };

    that.is_valid_name = function (name) {
        var valid_name = true;
        that.components.forEach(function (elem) {
            if (elem.name === name) {
                valid_name = false;
            }
        });
        return valid_name;
    };

    that.change_attribute = function (name, attribute, val) { //TODO: make it generic, xpath like stuff
        let n = that.find_node_named(name);
        if (n !== undefined) {
            if (attribute.indexOf("name") < 0 && attribute.indexOf("port") < 0) {
                n[attribute] = val;
            }
        }
    };

    that.change_attribute_link = function (name, attribute, val) { //TODO: make it generic, xpath like stuff
        let n = that.find_link_named(name);
        if (n !== undefined) {
            if (attribute.indexOf("name") < 0 && attribute.indexOf("port") < 0) {
                n[attribute] = val;
            }
        }
    };


    that.change_port_name_in_links = function (old_id, new_id) {

        var l = that.find_link_of_provided_port(old_id);
        if (l !== null) {
            l.src = new_id;
        }

        var l = that.find_link_of_required_port(old_id);
        if (l !== null) {
            l.target = new_id;
        }

        var ch = that.find_containment_of_required_port(old_id);
        if (ch !== null) {
            ch.target = new_id;
        }

        var c = that.find_containment_of_provided_port(old_id);
        if (c !== null) {
            c.src = new_id;
        }
    };

    that.change_name = function (name, comp) {
        if (that.is_valid_name(name)) {
            var oldname = comp.name;
            comp.name = name;

            //then we change name of all port_id
            comp.provided_communication_port.forEach(pc => {
                var l = that.find_link_of_provided_port('/' + oldname + '/' + pc.name);
                if (l !== null) {
                    l.src = that.generate_port_id(comp, pc);
                }
            });

            comp.required_communication_port.forEach(rc => {
                var l = that.find_link_of_required_port('/' + oldname + '/' + rc.name);
                if (l !== null) {
                    l.target = that.generate_port_id(comp, rc);
                }
            });

            var ch = that.find_containment_of_required_port('/' + oldname + '/' + comp.required_execution_port.name);
            if (ch !== null) {
                ch.target = that.generate_port_id(comp, comp.required_execution_port);
            }

            comp.provided_execution_port.forEach(pe => {
                var c = that.find_containment_of_provided_port('/' + oldname + '/' + pe.name);
                if (c !== null) {
                    c.src = that.generate_port_id(comp, pe);
                }
            });
        }
    };

    that.get_all_infra_component = function () {
        var tab = that.components.filter(function (elem) {
            if (elem._type.indexOf('infra') >= 0) {
                return elem;
            }
        });
        return tab;
    };

    that.is_valid_id = function (id) {
        var valid_id = true;
        that.components.forEach(function (elem) {
            if (elem.id === id) {
                valid_id = false;
            }
        });
        return valid_id;
    };


    that.find_target_port_of_link = function (l) {
        var resultat = undefined;
        var target_node_name = l.target.split('/')[1];
        console.log('>>>' + JSON.stringify(target_node_name));
        var the_target_node = that.find_node_named(target_node_name);
        console.log('>>>' + JSON.stringify(the_target_node));
        the_target_node.required_communication_port.forEach(function (elem) {
            if (that.get_port_name_from_port_id(l.target) === elem.name) {
                resultat = elem;
            }
        });
        return resultat;
    };

    that.find_src_port_of_link = function (l) {
        var resultat = undefined;
        var src_node_name = l.src.split('/')[1];
        var the_src_node = that.find_node_named(src_node_name);
        the_src_node.provided_communication_port.forEach(function (elem) {
            if (that.get_port_name_from_port_id(l.src) === elem.name) {
                resultat = elem;
            }
        });
        return resultat;
    };

    that.is_valid_with_errors = function () {
        var errors = [];

        for (var i = 0; i < that.components.length; i++) {
            for (var j = i + 1; j < that.components.length; j++) {
                if (that.components[i].name === that.components[j].name) {
                    errors.push("Names are not uniq! " + that.components[i].name);
                }
                if (that.components[i].id === that.components[j].id) {
                    errors.push("Ids are not uniq! " + that.components[i].id);
                }
            }
            if (that.components[i].required_communication_port !== undefined) {
                that.components[i].required_communication_port.forEach(function (elem) {
                    if (elem.isMandatory) {
                        if (that.find_link_of_required_port('/' + that.components[i].name + '/' + elem.name) === null) {
                            errors.push("Required port with Mandatory " + elem.name + " not linked!");
                        }
                    }
                });
            }
        }


        //Make sure all links relate to existing components && check capabilities
        that.links.forEach(function (elem) {
            var target_node_name = elem.target.split('/')[1];
            var src_node_name = elem.src.split('/')[1];
            var the_target_node = that.find_node_named(target_node_name);
            var the_src_node = that.find_node_named(src_node_name);
            if ((the_target_node === undefined) ||
                (the_src_node === undefined)) {
                errors.push("Src or target of " + elem.name + " does not exist!");
            } else {
                //Then let's check capabilities
                var tgt_port = that.find_target_port_of_link(elem);
                var src_port = that.find_src_port_of_link(elem);

                console.log(JSON.stringify(elem));
                console.log(JSON.stringify(tgt_port));
                console.log(JSON.stringify(src_port));

                if (tgt_port.capabilities !== undefined && tgt_port.capabilities.length > 0) {
                    if (src_port.capabilities !== undefined && src_port.capabilities.length > 0) {
                        errors.push("Capability " + elem.name + " is not satisfied!");
                    } else {
                        tgt_port.capabilities.forEach(function (cap) {
                            var res = false;
                            src_port.capabilities.forEach(function (car) {
                                if (cap._type.indexOf("security_capability") >= 0) {
                                    if (car.control_id === cap.control_id) {
                                        res = true;
                                    }
                                }
                                if (cap._type.indexOf("soft_capability") >= 0) {
                                    if (car.value === cap.value) {
                                        res = true;
                                    }
                                }
                                if (cap._type.indexOf("hardware_capability") >= 0) {
                                    if (car.connector === cap.connector && car.path === cap.path && car.permissions === cap.permission) {
                                        res = true;
                                    }
                                }
                            });
                            if (!res) {
                                errors.push("Capability " + cap.name + " of " + elem.name + " is not satisfied!");
                            }
                        });
                    }
                }
            }
        });

        //Make sure all hosted comps are on existing hosts.
        that.get_all_internals().forEach(function (elem) {
            var p_id = '/' + elem.name + '/' + elem.required_execution_port.name;
            var r = that.find_containment_of_required_port(p_id);
            if (r === null) {
                errors.push(elem.name + " has no host!");
            } else {
                var h_name = r.src.split('/')[1];
                if (that.find_node_named(h_name) === undefined) {
                    errors.push("Host " + r.src + " of " + elem.name + " does not exist!");
                }
            }
        });

        return errors;
    };

    that.is_valid = function () {
        var r = that.is_valid_with_errors();
        if (r.length > 0) {
            return false;
        } else {
            return true;
        }
    }

    return that;
}

/*****************************/
/*Credentials                  */
/*****************************/
var credentials = function (spec) {
    var that = {};
    that.username = spec.username || "ubuntu";
    that.password = spec.password || "";
    that.sshkey = spec.sshkey || "";
    that.agent = spec.agent || "";

    return that;
}

/*****************************/
/*Component                  */
/*****************************/

var component = function (spec) {
    var that = {};

    that._type = "";
    that.name = spec.name || 'a_component';
    that.properties = [];
    that.version = spec.version || "0.0.1";

    that.id = spec.id || uuidv4();

    var tab_pep = [];
    tab_pep.push(provided_execution_port({}));
    that.provided_execution_port = spec.provided_execution_port || tab_pep;

    that.add_property = function (prop) {
        that.properties.push(prop);
    };

    that.remove_property = function (prop) {
        var i = that.properties.indexOf(prop);
        if (i > -1) {
            that.properties.splice(i, 1); //The second parameter of splice is the number of elements to remove. Note that splice modifies the array in place and returns a new array containing the elements that have been removed. 
        }
    };

    that.get_all_properties = function () {
        var properties = [];
        for (var prop in that) {
            if (typeof that[prop] != 'function') {
                properties.push(prop);
            }
        }
        return properties;
    };

    return that;
};


/*****************************/
/*infrastructure_component   */
/*****************************/
var infrastructure_component = function (spec) {
    var that = component(spec); //the inheritance
    that._type += "/infra";
    that.ip = spec.ip || '127.0.0.1';
    that.port = spec.port || ['80', '22'];
    that.credentials = spec.credentials || credentials({});
    that.monitoring_agent = spec.monitoring_agent || "none";

    return that;
};


/*****************************/
/*Flow                       */
/*****************************/
var flow = function (spec) {
    var that = component(spec); //the inheritance
    that.path_to_flow = spec.path_to_flow || '/home/ubuntu';

    return that;
};

// To be completed - This is the list of things that should be extensible.
/*****************************/
/* Docker HOST               */
/*****************************/
var docker_host = function (spec) {
    var that = infrastructure_component(spec);
    that._type += "/docker_host";

    return that;
};

/*****************************/
/* VM Host                   */
/*****************************/
var vm_host = function (spec) {
    var that = infrastructure_component(spec);
    that._type += "/vm_host";

    return that;
};

/*****************************/
/*****************************/
var device = function (spec) {
    var that = infrastructure_component(spec);
    that._type += "/device";
    that.physical_port = spec.physical_port || "";
    that.device_type = spec.device_type || "";
    that.cpu = spec.cpu || "";
    that.needDeployer = spec.needDeployer || false;

    return that;
};




/**
 * Represent availability strategies such as replication or blue/green
 * deployment.
 * 
 * There are two basic strategies, namely 'builtin' and 'Docker
 * Swarm'.
 *
 *    - Selecting 'builtin' implies the deplyment of a separate proxy
 *      (i.e., NGinx) in front of the replicas and of watchdogs that
 *      monitor their health (using the provided health check
 *      script).
 *
 *    - Selecting 'DockerSwarm' implies that GeneSIS delegates to
 *      DockerSwarm the management of the replicas. Note that the
 *      health check script is not taken into account.
 */
class Availability {
    
    static defaultSettings () {
	return new Availability(this.DEFAULT, "", 1, true);
    }

    
    static fromObject (object) {
	return new Availability(object.strategy || this.DEFAULT,
				object.healthCheck || "",
				object.replicaCount || 1,
				object.zeroDownTime === null ? true : object.zeroDownTime,
				object.exposedPort || 80);
    }
    
    constructor (strategy, healthCheck, replicaCount, zeroDownTime, exposedPort) {
	this.strategy = strategy;
	this.healthCheck = healthCheck;
	this.replicaCount = replicaCount;
	this.zeroDownTime = zeroDownTime;
	this.exposedPort = exposedPort;
    }

    usesDockerSwarm () {
	return this.useStrategy(Availability.DOCKER_SWARM);
    }

    isBuiltin () {
	return this.useStrategy(Availability.BUILTIN);
    }

    useStrategy (strategy) {
	return this.strategy === strategy;
    }

    requireZeroDownTime() {
	return this.zeroDownTime;
    }
	
}


Availability.DOCKER_SWARM = "Docker Swarm";

Availability.BUILTIN = "Builtin";

Availability.DEFAULT = Availability.BUILTIN

/******************************************/
/* Software node (aka. Internal component)*/
/******************************************/
var software_node = function (spec) {
    var that = component(spec);

    // Check if the component  has an availability Policy
    that.hasAvailabilityPolicy = function ()  {
	return (that.availability !== null
		&& that.availability.strategy
		&& that.availability.replicaCount
		&& that.availability.zeroDownTime !== undefined);
    }

    
    // Check if the component includes a proper Docker resource
    that.hasDockerResource = function ()  {
	return (that.docker_resource !== null
		&& that.docker_resource.image
		&& that.docker_resource.command);
    }

    // Check if the component includes a proper SSH resource
    that.hasSSHResource = function () {
	return (that.ssh_resource !== null
		&& that.ssh_resource.startCommand
		&& that.ssh_resource.downloadCommand
		&& that.ssh_resource.installCommand);
    }
	
    
    that.availability = Availability.defaultSettings();
    if (spec.availability != null) {
	that.availability = Availability.fromObject(spec.availability);
    }
    
    that.docker_resource = spec.docker_resource || docker_resource({});
    that.ssh_resource = spec.ssh_resource || ssh_resource({});
    that.ansible_resource = spec.ansible_resource || ansible_resource({});
    that._type += "/internal";

    that.required_execution_port = spec.required_execution_port || required_execution_port({});

    var tab_pcp = [];
    tab_pcp.push(provided_communication_port({}));
    that.provided_communication_port = spec.provided_communication_port || tab_pcp;

    var tab_rcp = [];
    tab_rcp.push(required_communication_port({}));
    that.required_communication_port = spec.required_communication_port || tab_rcp;

    return that;
};



/******************************/
/* Specific Node-red component*/
/******************************/
var node_red = function (spec) {
    var that = software_node(spec); //the inheritance
    that._type += "/node_red";
    that.docker_resource = spec.docker_resource || docker_resource({
        image: "nicolasferry/multiarch-node-red-thingml:latest"
    });
    that.nr_flow = spec.nr_flow || [];
    that.path_flow = spec.path_flow || "";
    that.packages = spec.packages || [];

    if (spec.provided_communication_port === undefined) {
        that.provided_communication_port[0].port_number = '1880';
    }

    return that;
};


/*****************************/
/*External node              */
/*****************************/
var external_node = function (spec) {
    var that = component(spec); //the inheritance
    that._type += "/external";

    that.ip = spec.ip || '127.0.0.1';

    var tab_pcp = [];
    tab_pcp.push(provided_communication_port({}));
    that.provided_communication_port = spec.provided_communication_port || tab_pcp;

    var tab_rcp = [];
    tab_rcp.push(required_communication_port({}));
    that.required_communication_port = spec.required_communication_port || tab_rcp;

    return that;
};

/*****************************/
/*Link                       */
/*****************************/
var link = function (spec) {
    var that = {};
    that.name = spec.name || 'a_link';
    that.properties = [];
    that.src = spec.src || null;
    that.target = spec.target || null;
    that.isControl = spec.isControl || false;
    that.isDeployer = spec.isDeployer || false;

    return that;
}

/*****************************/
/*Hosting                    */
/*****************************/
var hosting = function (spec) {
    var that = {};
    that.name = spec.name || 'a_containment';
    that.properties = [];
    that.src = spec.src || null;
    that.target = spec.target || null;

    return that;
}

/*****************************/
/*Docker resource            */
/*****************************/
var docker_resource = function (spec) {
    var that = {};
    that.name = spec.name || uuidv4();
    that.image = spec.image || "ubuntu";
    that.command = spec.command || "";
    that.links = spec.links || [];
    that.extra_options = spec.extra_options || "";
    that.port_bindings = spec.port_bindings || {
        "1880": "1880"
    };
    that.devices = spec.devices || {
        "PathOnHost": '',
        "PathInContainer": '',
        "CgroupPermissions": "rwm"
    }

    return that;
}

/*****************************/
/*SSH resource               */
/*****************************/
var ssh_resource = function (spec) {
    var that = {};
    that.name = spec.name || uuidv4();
    that.startCommand = spec.startCommand || "";
    that.downloadCommand = spec.downloadCommand || "";
    that.installCommand = spec.installCommand || "";
    that.configureCommand = spec.configureCommand || "";
    that.stopCommand = spec.stopCommand || "";
    that.uploadCommand = spec.uploadCommand || [];
    that.credentials = spec.credentials || credentials({});

    return that;
};

/*****************************/
/*Ansible resource           */
/*****************************/
var ansible_resource = function (spec) {
    var that = {};
    that.name = spec.name || uuidv4();
    that.playbook_path = spec.playbook_path || "";
    that.playbook_host = spec.playbook_host || "";
    that.credentials = spec.credentials || credentials({});

    return that;
};


/*****************************/
/*Port                       */
/*****************************/
var port = function (spec) {
    var that = {};
    that.name = spec.name || uuidv4();
    that.capabilities = spec.capabilities || security_capability({});

    return that;
};



/*****************************/
/*ProvidedExecutionPort      */
/*****************************/
//These are for all components
var provided_execution_port = function (spec) {
    var that = port(spec);

    return that;
};


/*****************************/
/*RequiredExecutionPort      */
/*****************************/
//These are only for Software components
var required_execution_port = function (spec) {
    var that = port(spec);
    that.needDeployer = spec.needDeployer || false;

    return that;
};

/*****************************/
/*ProvidedCommunicationPort  */
/*****************************/
//These are only for Software components
var required_communication_port = function (spec) {
    var that = port(spec);
    that.port_number = spec.port_number || '80';
    that.isMandatory = spec.isMandatory || false;

    return that;
};

/*****************************/
/*RequiredCommunicationPort  */
/*****************************/
//These are only for Software components
var provided_communication_port = function (spec) {
    var that = port(spec);
    that.port_number = spec.port_number || '80';

    return that;
};

/*****************************/
/*Capabilities               */
/*****************************/
var capability = function (spec) {
    var that = {};
    that._type = "/capability";
    that.name = spec.name || 'a_capability';

    return that;
};

/*****************************/
/*S&P Capability             */
/*****************************/
var security_capability = function (spec) {
    var that = capability(spec);
    that._type += "/security_capability";
    //https://nvlpubs.nist.gov/nistpubs/SpecialPublications/NIST.SP.800-53r4.pdf
    that.control_id = spec.control_id || "";
    that.description = spec.description || "";

    return that;
};

/*****************************/
/*Hardware Capability        */
/*****************************/
var hardware_capability = function (spec) {
    var that = capability(spec);
    that._type += "/hardware_capability";
    that.connector = spec.connector || "GPIO";
    that.path = spec.path || "/dev/pigpio";
    that.permissions = spec.permission || "rwx";

    return that;
};

/*****************************/
/*Soft Capability            */
/*****************************/
var soft_capability = function (spec) {
    var that = capability(spec);
    that._type += "/soft_capability";
    that.value = spec.connector || "";

    return that;
};



////////////////////////////////////////////////
module.exports = {
    deployment_model: deployment_model,
    ssh_resource: ssh_resource,
    ansible_resource: ansible_resource,
    docker_resource: docker_resource,
    link: link,
    external_node: external_node,
    node_red: node_red,
    software_node: software_node,
    device: device,
    vm_host: vm_host,
    docker_host: docker_host,
    flow: flow,
    infrastructure_component: infrastructure_component,
    component: component,
    port: port,
    provided_communication_port: provided_communication_port,
    required_communication_port: required_communication_port,
    required_execution_port: required_execution_port,
    provided_execution_port: provided_execution_port,
    hosting: hosting,
    security_capability: security_capability,
    hardware_capability: hardware_capability,
    soft_capability: soft_capability
}

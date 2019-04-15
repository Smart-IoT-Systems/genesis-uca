/*****************************/
/*Deployment model           */
/*Contain all nodes and links*/
/*****************************/

var deployment_model = function (spec) {
    var that = {};
    that.name = spec.name || 'a _deployment_name';
    that.components = [];
    that.links = [];
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
                    if (that.type_registry[i].id.indexOf(type) >= 0) { //To be updated
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

    that.remove_component = function (component) {
        var i = that.components.indexOf(component); // This could be factorized in the array.prototype
        if (i > -1) {
            that.components.splice(i, 1); //The second parameter of splice is the number of elements to remove. Note that splice modifies the array in place and returns a new array containing the elements that have been removed. 
        }
        //we also need to remove the associated links
        var tab_indexes = [];
        for (var i in that.links) {
            if (that.links[i].target === component.name ||
                that.links[i].src === component.name) {
                tab_indexes.push(i);
            }
        }
        if (tab_indexes.length > 0) {
            tab_indexes.forEach(function () {
                that.links.splice(elem, 1);
            });
        }
    };

    that.remove_link = function (link) {
        var i = that.links.indexOf(link);
        if (i > -1) {
            that.links.splice(i, 1);
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

    that.get_all_hosts = function () {
        var tab = that.components.filter(function (elem) {
            if (!elem.hasOwnProperty('id_host')) {
                return elem;
            }
        });
        return tab;
    };

    that.get_hosted = function (name) {
        var tab = that.components.filter(function (elem) {
            if (elem.id_host === name) {
                return elem;
            }
        });
        return tab;
    };

    that.get_all_hosted = function () {
        var tab = that.components.filter(function (elem) {
            if (elem.hasOwnProperty('id_host')) {
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
            if (elem.target === comp.name) {
                return elem;
            }
        });
        return tab;
    };

    that.get_all_outputs_of_component = function (comp) {
        var tab = that.links.filter(function (elem) {
            if (elem.src === comp.name) {
                return elem;
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

    that.is_valid_name = function (name) {
        var valid_name = true;
        that.components.forEach(function (elem) {
            if (elem.name === name) {
                valid_name = false;
            }
        });
        return valid_name;
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
        }

        //Make sure all links relate to existing components
        that.links.forEach(function (elem) {
            if ((that.find_node_named(elem.src) === undefined) ||
                (that.find_node_named(elem.target) === undefined)) {
                errors.push("Src or target of " + elem.name + " does not exist!");
            }
        });

        //Make sure all hosted comps are on existing hosts.
        that.get_all_hosted().forEach(function (elem) {
            if(elem._type.indexOf('external') === -1){ //External does not have host
                if (that.find_node_named(elem.id_host) === undefined) {
                    errors.push("Host " + elem.id_host + " of " + elem.name + " does not exist!");
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
    that.password = spec.password || "ubuntu";
    that.sshkey = spec.sshkey || "";

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

    that.id = spec.id || 'a_unique_id';

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
    that.isLocal = spec.isLocal || false;

    return that;
};

/******************************************/
/* Software node (aka. Internal component)*/
/******************************************/
var software_node = function (spec) {
    var that = component(spec);
    that.id_host = spec.id_host || null;
    that.docker_resource = spec.docker_resource || docker_resource({});
    that.ssh_resource = spec.ssh_resource || ssh_resource({});
    that.ansible_resource = spec.ansible_resource || ansible_resource({});
    that._type += "/internal";
    that.port = spec.port || '1880';

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

    return that;
};


/*****************************/
/*External node              */
/*****************************/
var external_node = function (spec) {
    var that = component(spec); //the inheritance
    that._type += "/external";

    return that;
};

/*****************************/
/*Link                       */
/*****************************/
var link = function (spec) {
    var that = component(spec);
    that.src = spec.src || null;
    that.target = spec.target || null;
    that.isControl = spec.isControl || false;
    that.isMandatory = spec.isMandatory || false;
    that.isDeployer = spec.isDeployer || false;

    return that;
}

/*****************************/
/*Docker resource            */
/*****************************/
var docker_resource = function (spec) {
    var that = {};
    that.name = spec.name || "a resource";
    that.image = spec.image || "ubuntu";
    that.command = spec.command || "";
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
    that.name = spec.name || "a resource";
    that.startCommand = spec.startCommand || "";
    that.downloadCommand = spec.downloadCommand || "";
    that.installCommand = spec.installCommand || "";
    that.configureCommand = spec.configureCommand || "";
    that.credentials = spec.credentials || credentials({});

    return that;
}

/*****************************/
/*Ansible resource           */
/*****************************/
var ansible_resource = function (spec) {
    var that = {};
    that.name = spec.name || "a resource";
    that.playbook_path = spec.playbook_path || "";
    that.playbook_host = spec.playbook_path || "";
    that.credentials = spec.credentials || credentials({});

    return that;
}



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
    component: component
}
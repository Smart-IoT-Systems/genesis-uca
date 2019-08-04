var _ = require('lodash');
var logger = require('./logger.js');

const useless_properties = ["add_property","remove_property","get_all_properties",'_runtime', 'container_id'];

var comparator = function (dm) {
    var that = {};
    that.dm = dm;

    that.compare = function (target_dm) {
        var result = {};
        result.list_removed_hosts = [];
        result.list_of_added_hosts = [];

        result.list_of_removed_components = [];
        result.list_of_added_components = [];

        result.list_of_added_hosted_components = [];

        result.list_of_removed_links = [];
        result.list_of_added_links = [];

        result.list_of_removed_containments = [];
        result.list_of_added_containments = [];

        result.list_of_added_links_deployer = [];

        result.old_dm = that.dm;

        //Added Hosts and components
        var target_comps = target_dm.components;
        for (var i in target_comps) {
            var tmp_node = dm.find_node_named(target_comps[i].name);
            if (tmp_node === undefined) { 
                console.log(target_comps[i].name + " is not in old model so it has been added");
                if (target_comps[i]._type.indexOf('internal') > -1) {
                    if (target_dm.find_host(target_comps[i]) !== null) {
                        result.list_of_added_hosted_components.push(target_comps[i]);
                    }
                    result.list_of_added_components.push(target_comps[i]);
                } else {
                    result.list_of_added_hosts.push(target_comps[i]);
                }
            } else if (tmp_node.name === target_comps[i].name) {
                var r = that.compareObjects(_.omit(tmp_node, useless_properties), _.omit(target_comps[i], useless_properties));
                if (r.length > 0) { // There are some differences
                    if (target_comps[i]._type.indexOf('internal') > -1) {
                        if (target_dm.find_host(target_comps[i]) !== null) {
                            result.list_of_added_hosted_components.push(target_comps[i]);
                        }
                        result.list_of_added_components.push(target_comps[i]);
                    } else {
                        result.list_of_added_hosts.push(target_comps[i]);
                    }
                }
            }
        }

        //Removed hosts and components
        var comps = dm.components;
        for (var i in comps) {
            var tmp_host = target_dm.find_node_named(comps[i].name);
            if (tmp_host === undefined) {
                console.log(comps[i].name + " is not in the target model so it has been removed!");
                if (comps[i]._type.indexOf('internal') > -1) {
                    result.list_of_removed_components.push(comps[i]);
                } else {
                    result.list_removed_hosts.push(comps[i]);
                }
            } else if (tmp_host.name === comps[i].name) {
                var r = that.compareObjects(_.omit(tmp_host, useless_properties), _.omit(comps[i], useless_properties));
                if (r.length > 0) { // There are some differences
                    if (comps[i]._type.indexOf('internal') > -1) {
                        result.list_of_removed_components.push(comps[i]);
                    } else {
                        result.list_removed_hosts.push(comps[i]);
                    }
                }
            }
        }

        //Added links
        var target_links = target_dm.links;
        for (var i in target_links) {
            var tmp_link = dm.find_link_named(target_links[i].name);
            if (tmp_link === undefined) {
                if (target_links[i].isDeployer) {
                    result.list_of_added_links_deployer.push(target_links[i]);
                } else {
                    result.list_of_added_links.push(target_links[i]);
                }
            }
        }

        //Removed links
        var links = dm.links;
        for (var i in links) {
            var tmp_link = target_dm.find_link_named(links[i].name);
            if (tmp_link === undefined) {
                result.list_of_removed_links.push(links[i]);
            }
        }

        //Added containments
        var conts = target_dm.containments;
        for (var i in conts) {
            var tmp_cont = dm.find_containment_named(conts[i].name);
            if (tmp_cont === undefined) {
                result.list_of_added_containments.push(conts[i])
            }
        }

        //Removed containments
        var re_conts = dm.containments;
        for (var i in re_conts) {
            var tmp_cont = target_dm.find_containment_named(re_conts[i].name);
            if (tmp_cont === undefined) {
                result.list_of_removed_containments.push(re_conts[i])
            }
        }

        logger.log("info", "Added Components:" + JSON.stringify(result.list_of_added_components));
        logger.log("info", "Removed Components:" + JSON.stringify(result.list_of_removed_components));
        logger.log("info", "Removed Hosts:" + JSON.stringify(result.list_removed_hosts));

        return result;
    };

    that.compareObjects = function (obj1, obj2) {
        const diff = Object.keys(obj1).reduce((result, key) => {
            if (!obj2.hasOwnProperty(key)) {
                result.push(key);
            } else if (_.isEqual(obj1[key], obj2[key])) {
                const resultKeyIndex = result.indexOf(key);
                result.splice(resultKeyIndex, 1);
            }
            return result;
        }, Object.keys(obj2));
    
        return diff;
    }

    return that;
};

module.exports = comparator;
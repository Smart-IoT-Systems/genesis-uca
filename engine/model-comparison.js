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

        result.list_of_added_links_deployer = [];

        //Added Hosts and components
        var target_comps = target_dm.components;
        for (var i in target_comps) {
            var tmp_node = dm.find_node_named(target_comps[i].name);
            if (tmp_node === undefined) {
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

        //Removed hosts and components
        var comps = dm.components;
        for (var i in comps) {
            var tmp_host = target_dm.find_node_named(comps[i].name);
            console.log(tmp_host);
            if (tmp_host === undefined) {
                if (comps[i]._type.indexOf('internal')) {
                    result.list_of_removed_components.push(comps[i]);
                } else {
                    result.list_removed_hosts.push(comps[i]);
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

        return result;
    };


    return that;
};

module.exports = comparator;
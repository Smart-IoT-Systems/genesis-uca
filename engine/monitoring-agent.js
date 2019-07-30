
var dc = require('./connectors/docker-connector.js');

var monitoring_agent = function (host, level) {
    var that={};
    that.host=host;
    that.level=level;

    that.start = async function(){

    };

    that.stop = async function(){

    };

    that.remove = async function(){
        var connector = dc();

        await connector.stopAndRemove("netdata", that.host.ip, that.host.port);
    }

    that.install = async function(){
        var connector = dc();

        connector.add_extra_options(["CapAdd", ["SYS_PTRACE"]]);
        connector.add_extra_options(["SecurityOpt", ["apparmor=unconfined"]]);

        var id = await connector.buildAndDeploy(that.host.ip, that.host.port, {"19999":"19999"}, undefined, "", "netdata/netdata:latest",
         [{"src":"/proc", "tgt":"/host/proc"}, {"src":"/sys", "tgt":"/host/sys"}, {"src":"/var/run/docker.sock", "tgt":"/var/run/docker.sock"}], "", "netdata", that.host.name);
    };

    return that;
};

module.exports = monitoring_agent;
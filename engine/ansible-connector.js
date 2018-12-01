var logger = require('./logger.js');
var ansible = require('node-ansible-wrapper');
var emitter = require('events').EventEmitter;
var fs = require('fs');

var ansible_connector = function (tgt_node, ansible_resource) {

    var that = new emitter();
    that.tgt_node = tgt_node;
    that.ansible_resource = ansible_resource;


    that.prepareInventory = function () {
        var host = "[" + that.ansible_resource.playbook_host + "]\n";
        host += that.tgt_node.ip + " ansible_connection=ssh ansible_user=" + that.ansible_resource.credentials.username + " ansible_ssh_private_key_file=" + that.ansible_resource.credentials.sshkey;
        fs.writeFileSync('./hosts', host);
    };

    that.executePlaybook = function () {
        that.prepareInventory();
        var playbook = new ansible.Playbook().playbook(ansible_resource.playbook_path).inventory("./hosts");;
        playbook.on('stdout', function (data) {
            logger.log("info", data.toString());
        });
        playbook.on('stderr', function (data) {
            logger.log("info", data.toString());
        });
        var promise = playbook.exec();
    };

    return that;
};

module.exports = ansible_connector;
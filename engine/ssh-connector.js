var Client = require('ssh2').Client;
var fs = require('fs');
var emitter = require('events').EventEmitter;
var logger=require('./logger.js');

var ssh_connector = function () {

    var that = new emitter();

    that.executeCommand = function (ip, port, username, key, command) {
        var conn = new Client();
        conn.on('ready', function() {
        logger.log('info','SSH Client :: ready');
        conn.exec(command, function(err, stream) {
            if (err) throw err;
            stream.on('close', function(code, signal) {
                logger.log('info','Stream :: close :: code: ' + code + ', signal: ' + signal);
                conn.end();
            }).on('data', function(data) {
                logger.log('info','STDOUT: ' + data);
            }).stderr.on('data', function(data) {
                logger.log('error','STDERR: ' + data);
            });
        });
        }).connect({
            host: ip,
            port: port,
            username: username,
            privateKey: fs.readFileSync(key)
        });
    };


    return that;
};

module.exports = ssh_connector;
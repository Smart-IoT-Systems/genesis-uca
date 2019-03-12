var Client = require('ssh2').Client;
var fs = require('fs');
var logger = require('./logger.js');
const SSH2Utils = require('ssh2-utils');

var ssh_connector = function (ip, port, username, passwd, key) {

    var that = {};
    that.options={
        host: ip,
        port: parseInt(port),
        username: username
    };
    if(key !== ""){
        that.options.privateKey= fs.readFileSync(key);
    }else{
        that.options.password=passwd;
    }


    that.upload_directory= function(src_dir, target_dir){
        return new Promise(function (resolve, reject) {
            var sftp_util = new SSH2Utils();
            sftp_util.putDir(that.options,src_dir,target_dir, function(err){
                if(err) {
                    logger.log('error',err);
                        reject(err);
                        throw err;
                }else{
                    resolve(target_dir);
                }
            });
        });
    };

    that.upload_file = function (file_path_src, file_path_tgt) {
        return new Promise(function (resolve, reject) {
            var conn = new Client();
            conn.on('ready', function () {
                conn.sftp(function (err, sftp) {
                    if (err) {
                        logger.log('error',err);
                        reject(err);
                        throw err;
                    };
                    var readStream = fs.createReadStream(file_path_src);
                    var writeStream = sftp.createWriteStream(file_path_tgt);

                    writeStream.on('close', function () {
                        logger.log('info', "file transferred succesfully");
                        resolve(file_path_tgt);
                    });

                    writeStream.on('end', function () {
                        conn.close();
                    });

                    readStream.pipe(writeStream);
                });
            }).connect(that.options);
        });
    };

    that.execute_command = function (command) {
        return new Promise(function (resolve, reject) {
            var conn = new Client();
            conn.on('ready', function () {
                conn.exec(command, function (err, stream) {
                    if (err){
                        logger.log('error',err);
                        reject(err);
                        throw err;
                    };
                    stream.on('close', function (code, signal) {
                        logger.log('info', 'Stream :: close :: code: ' + code + ', signal: ' + signal);
                        conn.end();
                        resolve(signal);
                    }).on('data', function (data) {
                        logger.log('info', 'ssh: ' + data);
                    }).stderr.on('data', function (data) {
                        logger.log('error', 'ssh: ' + data);
                    });
                });
            }).connect(that.options);
        });
    };


    return that;
};

module.exports = ssh_connector;
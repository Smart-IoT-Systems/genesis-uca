var Client = require('ssh2').Client;
var fs = require('fs');
var logger = require('../logger.js');
var tar = require('tar');

var ssh_connector = function (ip, port, username, passwd, key, agent) {

    var that = {};
    that.options = {
        host: ip,
        port: parseInt(port),
        username: username
    };
    if (key !== "") {
        that.options.privateKey = fs.readFileSync(key);
    } else if (agent !== "" && agent !== undefined) {
        that.options.agent = agent;
    } else {
        that.options.password = passwd;
    }


    that.upload_directory = function (src_dir, target_dir) {
        return new Promise(function (resolve, reject) {
            tar.c( // or tar.create
                {
                  gzip: true
                },
                [src_dir]
            ).pipe(fs.createWriteStream(src_dir+'.tgz'));

            that.upload_file(src_dir + '.tgz', target_dir+'.tgz').then(function () {
                that.execute_command("tar xf " + target_dir + '.tgz').then(function () {
                    resolve();
                }).catch(function (e) {
                    reject(e)
                });
            }).catch(function (e) {
                reject(e)
            });
        });
    };

    that.upload_file = function (file_path_src, file_path_tgt) {
        console.log(">>>>>>>>"+ file_path_src + "  ::: "+JSON.stringify(that.options));
        return new Promise(function (resolve, reject) {
            if(file_path_src !== undefined && file_path_tgt !== undefined){
                var conn = new Client();
                conn.on('ready', function () {
                    conn.sftp(function (err, sftp) {
                        if (err) {
                            logger.log('error', err);
                            reject(err);
                            throw err;
                        };
                        console.log(">>>>>>>>"+ file_path_src + "  ::: "+that.options);
                        var readStream = fs.createReadStream(file_path_src);
                        var writeStream = sftp.createWriteStream(file_path_tgt);

                        writeStream.on('close', function () {
                            logger.log('info', "file transferred successfully");
                            resolve(file_path_tgt);
                        });

                        writeStream.on('end', function () {
                            conn.close();
                        });

                        readStream.pipe(writeStream);
                    });
                }).connect(that.options);


            }else{
                resolve(undefined);
            }
        });
    };

    that.set_env_var = function(var_name, value){
        return new Promise(function (resolve, reject) {
            var caommad="sudo sh -c 'echo export "+var_name+"="+value+" >> /etc/environment'";
            that.execute_command(caommad).then(function(){
                resolve(signal);
            }).catch(function(err){
                reject(err);
            });
        });
    };

    that.execute_command = function (command) {
        console.log(`Inside execute command: ${command}`);
        return new Promise(function (resolve, reject) {
            const conn = new Client();
            conn
                .on('error', (error) => {
                    logger.error(error);
                    reject(error);
                })
                .on('ready', function () {
                    console.log("Connection ready");
                    conn.exec(command, {pty: false}, function (err, stream) {
                        if (err) {
                            console.log("Step Error while establishing the connection.");
                            logger.log('error', err);
                            reject(err);
                            return;
                        };
                        stream.pipe(process.stdout, {end: true});
                        stream.stderr.pipe(process.stderr, {end: true});
                        stream.on('close', function (code, signal) {
                            // logger.info("Received close");
                            conn.end();
                            if (code != 0) {
                                logger.error(`Command failed (code: ${code} / ${signal}`);
                                reject(signal);
                                return;
                            }
                            logger.info(`Command successful (code: ${code})`);
                            resolve(code);
                        });
                        // stream.on('end', () => {
                        //     console.log("Stream received 'end.'");
                        // });
                        // stream.on('finish', () => {
                        //     console.log('Received finish');
                        // });
                        stream.on('error', (error) => {
                            console.log("Stream received error.");
                            reject(error);
                        });
                        //stream.on('data', function (data) {
                        //     logger.log('info', 'ssh: ' + data);
                        //});
                        // stream.stderr.on('data', function (data) {
                        //     process.stderr.write(data);
                        // });
                });
            }).connect(that.options);
        });
    };


    return that;
};

module.exports = ssh_connector;

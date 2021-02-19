//This function call ThingML for compilation
var spawn = require('child_process').spawn;
var fs = require('fs');
var path = require('path');
var AdmZip = require('adm-zip');
var logger = require('./logger.js');
var rimraf = require("rimraf");

var thingml_compiler = function (node) {
    var that = {};
    that.thingml_node = node;

    that.cleanDirectory = function (dirpath) {
        if (!fs.existsSync(dirpath)) {
            return;
        }
        if (!fs.lstatSync(dirpath).isDirectory()) {
            fs.unlinkSync(dirpath);
            return;
        }

        /*var files = fs.readdirSync(dirpath);
        if (files) {
            files.forEach(function (file) {
                var filepath = path.resolve(dirpath, file);
                if (fs.lstatSync(filepath).isDirectory()) {
                    that.cleanDirectory(filepath);
                    fs.rmdirSync(filepath);
                } else {
                    fs.unlinkSync(filepath);
                }
            });
        }*/

        rimraf.sync(dirpath);
    };

    that.unzipSources = function (source, callback) {
        try {
            var zip = new AdmZip(source);
            zip.extractAllTo('./', true);
        } catch (e) { // zip file missing or corrupted
            this.emitter.emit('error', e);
            return;
        }
        callback();
    };

    that.spawnCompiler = function (target, source, output) {
        return new Promise(function (resolve, reject) {
            var hasError = false;

            var process = spawn('java', [
                '-jar', __dirname + '/' + '../lib/thingml/ThingML2CLI.jar',
                '-c', target,
                '-s', source,
                '-o', output
            ]);

            process.stdout.setEncoding('utf8');
            process.stdout.on('data', (data) => {
                data.trim().split('\n').forEach(line => {
                    if (line.toLowerCase().startsWith('warning')) {
                        logger.log("warn", line);
                    } else if (line.toLowerCase().startsWith('error')) {
                        hasError = true;
                        logger.log("error", line);
                    } else {
                        logger.log("info", line);
                    }
                });
            });

            process.stderr.setEncoding('utf-8');
            process.stderr.on('data', (data) => {
                data.trim().split('\n').forEach(line => {
                    if (!line.toLowerCase().startsWith('warning')) {
                        logger.log("warn", line);
                    } else if (line.toLowerCase().startsWith('error')) {
                        hasError = true;
                        logger.log("error", line);
                    } else {
                        logger.log("info", data);
                    }
                });
            });

            process.on('error', (err) => {
                hasError = true;
                logger.log("error", 'Something went wrong with the compiler! ' + err);
            });

            process.on('exit', (code) => {
                if (code !== 0) {
                    hasError = true;
                    logger.log("error", "Error code: " + code);
                    reject(err);
                }
                if (hasError) {
                    logger.log("error", 'Cannot complete because of errors!');
                } else {
                    logger.log("info", 'Done!');
                }
                delete process;
                resolve(that);
            });
        });
    };

    that.build = function (output, target) {
        return new Promise(function (resolve, reject) {
            that.cleanDirectory(output);

            if (that.thingml_node.file === '') {
                var destFile = './generated/' + that.thingml_node.name + ".thingml";
                fs.writeFile(destFile, that.thingml_node.src, function (err) {
                    if (err) {
                        return logger.log("error", err);
                    }
                    logger.log("info", "The ThingML was saved as " + destFile);
                });
                that.spawnCompiler(target, destFile, output).then(function (elem) {
                        resolve(elem);
                    },
                    function (err) {
                        reject(elem);
                    });
            } else {
                that.spawnCompiler(target, that.thingml_node.file, output).then(function (elem) {
                    resolve(elem);
                }, function (err) {
                    reject(elem);
                });
            }
        });
    }

    return that;
};

module.exports = thingml_compiler;
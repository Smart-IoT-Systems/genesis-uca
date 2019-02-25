var logger = require('./logger.js');
var maven = require('node-maven-api');

var maven_builder = function (path) {
    var that = {};
    that.mvn = maven.create(path);

    that.clean = function () {
        return new Promise(function (resolve, reject) {
            that.mvn.clean();
            that.mvn.registerEvent('clean', () => {
                resolve();
            });
            that.mvn.registerEvent('clean-failed', (err) => {
                reject(err);
            });
        });
    };

    that.install = function () {
        return new Promise(function (resolve, reject) {
            that.mvn.install();
            that.mvn.registerEvent('install', () => {
                resolve();
            });
            that.mvn.registerEvent('install-failed', (err) => {
                reject(err);
            });
        });
    };

    that.clean_install = function () { //hum this is super ugly ...
        return new Promise(function (resolve, reject) {
            that.clean().then(function(){
                that.install().then(function(){
                    resolve();
                }).catch(function(){
                    reject();
                });
            }).catch(function(err){
                reject();
            });
        });
    }

    return that;
};

module.exports = maven_builder;
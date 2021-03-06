var mm = require('../metamodel/allinone.js');

/******************************/
/* Specific jCasbin RBAC      */
/******************************/
var jcasbin = function (spec) {
    var that = mm.software_node(spec);
    that._type += "/jcasbin";
    that.name = spec.name || "jCasbin";
    that.ssh_resource = spec.ssh_resource || {
        "name": "52a12bfa-075f-44d9-9358-f55booodcc36e5",
        "startCommand": "cd  jcasbin; mvn spring-boot:run -Dspring-boot.run.arguments='--casbin.authorization.model=casbinFiles\\rbac_model.conf --casbin.authorization.policy=casbinFiles\\rbac_policy.csv --server.port=8011' &> jcasbin.txt",
        "downloadCommand": "mkdir jcasbin; cd jcasbin; wget -L https://gitlab.com/enact/GeneSIS/-/raw/master/docs/smartbuilding/spring-boot-microservice-jcasbin.zip?inline=false -O spring-boot-microservice-jcasbin.zip; unzip spring-boot-microservice-jcasbin.zip",
        "installCommand": "sudo apt-get update; sudo apt-get install -y maven",
        "configureCommand": "",
        "stopCommand": "",
        "credentials": {
            "username": "pi",
            "password": "raspberry",
            "sshkey": "",
            "agent": ""
        }
    };

    return that;
};

module.exports = jcasbin;

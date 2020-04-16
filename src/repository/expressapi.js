var mm = require('../metamodel/allinone.js');

/******************************/
/* Specific ExpressAPIGateway */
/******************************/
var expressapi = function (spec) {
    var that = mm.software_node(spec);
    that._type += "/expressapi";
    that.name = spec.name || "ExpressAPI";
    that.ssh_resource= spec.ssh_resource || {
        "name": "52a12bfa-075f-44d9-9358-f55b9dcc36e5",
        "uploadCommand": ["/Users/ferrynico/Downloads/20200106_GeneSIS4SP_Code/apigw/config/gateway.config.yml","/home/pi/gateway.config.yml"],
        "startCommand": "nohup npm start",
        "downloadCommand": "wget -L https://www.dropbox.com/s/6kgfusgxolp2bnm/APIGW.zip?dl=0 -O APIGW.zip",
        "installCommand": "unzip APIGW.zip && npm install",
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

module.exports = expressapi;

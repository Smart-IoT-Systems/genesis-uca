var mqtt = require('mqtt');
var fs = require('fs');


var KEY = fs.readFileSync('/Users/ferrynico/Documents/Code/INDRAMQTT/private_key/nicolas.key.pem');
var CERT = fs.readFileSync('/Users/ferrynico/Documents/Code/INDRAMQTT/public_key/nicolas.cert.pem');
//var CAfile = [fs.readFileSync('/Users/ferrynico/Documents/Code/INDRAMQTT/public_key/extca-chain.cert.pem', 'utf8')];
var CAfile = [fs.readFileSync('/Users/ferrynico/Documents/Code/INDRAMQTT/public_key/extca-chain.cert.pem')];

var options = {
    port: 8883,
    hostname: 'cmw.ext.innovarail.indra.es',
    ca: CAfile,
    key: KEY,
    cert: CERT,
    secureProtocol: 'TLS_method',
    rejectUnauthorized : false,
    protocol: 'mqtts',
    protocolId: 'MQTT',
    protocolVersion: 4,
    clientId: 'genesis',
    username: 'nicolas',
    password: 'ENACT9012'
};


var client  = mqtt.connect('mqtts://cmw.ext.innovarail.indra.es',options);

client.on("connect",function(){	
    console.log("connected  :) ");

    client.subscribe('#', function (err) {
        console.log("Subscribed");
    });
    

    client.on('message', function (topic, message) {
        console.log(message.toString());
      });
});

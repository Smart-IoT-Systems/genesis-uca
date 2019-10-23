var runtime = require('./engine/engine.js');
var broker = require('./engine/MQTTBroker.js');
var logger=require('./engine/logger.js');
var express = require("express");
var app = express();
var bodyParser = require('body-parser')
var http = require('http');
var fs = require('fs');
var bus = require('./engine/event-bus.js');
var mm = require('./metamodel/allinone.js');

app.set("port", 8000);
app.use(bodyParser.json({limit: '50mb'}));
app.use(bodyParser.urlencoded({limit: '50mb',  extended: true }))


//Start the engine
logger.log('info','Engine started!');
runtime.start();
broker.start();

// start server
var server = app.listen(app.get('port'), '0.0.0.0', function () {
	var port = server.address().port;
   logger.log('info','GeneSIS Engine API started on ' + port);
}); 

app.post("/genesis/deploy", runtime.deploy);

var file= fs.readFileSync(process.env.npm_config_path);
var model = JSON.parse(file);
triggerTest(JSON.stringify(model));

var tested=1; 

bus.on('deployment-completed', function(){
    if(tested > 0){
        tested--;
        console.log("Time to remove all!!!");
        var empty_model=mm.deployment_model({});
        triggerTest(JSON.stringify(empty_model));
    }else{
        process.exit(0);
    }
});

function triggerTest(post_data){
    var post_options = {
        host: '127.0.0.1',
        port: '8000',
        path: '/genesis/deploy',
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Content-Length': Buffer.byteLength(post_data)
        }
    };
    
    var post_req = http.request(post_options, function(res) {
        res.setEncoding('utf8');
        res.on('data', function (chunk) {
            console.log('Response: ' + chunk);
        });
    });
    
    // post the data
    post_req.write(post_data);
    post_req.end();
}
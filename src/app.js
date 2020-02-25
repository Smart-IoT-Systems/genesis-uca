var runtime = require('./engine/engine.js');
var broker = require('./engine/MQTTBroker.js');
var puml = require('./engine/connectors/plantuml.js');
var logger=require('./engine/logger.js');
var express = require("express");
var app = express();
var fs = require('fs');
var bodyParser = require('body-parser')
var swaggerUi = require('swagger-ui-express');
var swaggerDocument = require('./swagger.json');
var http = require('http');

app.set("port", 8000);
app.use(bodyParser.json({limit: '50mb'}));
app.use(bodyParser.urlencoded({limit: '50mb',  extended: true }))


//Start the engine
logger.log('info','Engine started!');
runtime.start();
broker.start();
puml.start();

// start server
var server = app.listen(app.get('port'), '0.0.0.0', function () {
	var port = server.address().port;
   logger.log('info','GeneSIS Engine API started on ' + port);
}); 


//Retrieve all component types registered in the engine
app.get("/genesis/types", runtime.getTypes);
//Initiate deployment
app.post("/genesis/deploy", runtime.deploy);
//Send the server logs
app.get("/genesis/logs", getLogs);
//Send the current deployment model
app.get("/genesis/model", runtime.getDM);
//Send the current deployment model with graph
app.get("/genesis/model_ui", runtime.getDM_UI);
//Update attribute of a component
app.post("/genesis/component", runtime.update_component);
//Trigger a deployment of the model in memory
app.get("/genesis/deploy_model", runtime.push_model);
//Update target model in memory
app.post("/genesis/push_model", runtime.update_target_model);
//Retrieve target model
app.get("/genesis/get_target_model", runtime.get_targetDM);
//Description of the GeneSIS API
app.use('/api-docs', swaggerUi.serve, swaggerUi.setup(swaggerDocument));
//Trigger runtime info update
app.get('/genesis/runtime_info', runtime.getRuntime_info);

function getLogs(req, res){
    var contents = fs.readFileSync(__dirname+'/genesis.log', 'utf8');
    res.end(contents);
}



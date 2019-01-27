var plantuml = require('node-plantuml');
var express = require('express');
var logger = require('./logger.js');
var fs = require("fs");

var plantuml_generator=(function(){
    var that={};

    that.start=function(){

        var app = express();

        plantuml.useNailgun(); // Activate the usage of Nailgun
 
        app.get('/png/:uml', function(req, res) {
          res.set('Content-Type', 'image/png');
          logger.log("info", 'Get Plantuml');
         
          //var decode = plantuml.decode(req.params.uml);
          var readFile = fs.createReadStream("./generated/"+req.params.uml+"/docs/"+req.params.uml+".plantuml");
          var gen = plantuml.generate({format: 'png'});
         
          //decode.out.pipe(gen.in);
          readFile.pipe(gen.in);
          gen.out.pipe(res);
        });
         
        app.get('/svg/:uml', function(req, res) {
          res.set('Content-Type', 'image/svg+xml');
         
          //var decode = plantuml.decode(req.params.uml);
          var readFile = fs.createReadStream("./generated/"+req.params.uml+"/docs/"+req.params.uml+".plantuml");
          var gen = plantuml.generate({format: 'svg'});
         
          //decode.out.pipe(gen.in);
          readFile.pipe(gen.in);
          gen.out.pipe(res);
        });
        
        logger.log("info", 'PlantUML diagram generator started on port: '+8080);
        app.listen(8080);
    };

    return that;
}());

module.exports = plantuml_generator;
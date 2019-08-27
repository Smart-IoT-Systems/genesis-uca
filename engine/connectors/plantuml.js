var plantuml = require('node-plantuml');
var express = require('express');
var logger = require('../logger.js');
var fs = require("fs");

var plantuml_generator=(function(){
    var that={};

    that.start=function(){

        var app = express();
        app.use(function(req, res, next) {
          res.header("Access-Control-Allow-Origin", "*");
          res.header("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");
          next();
        });

        plantuml.useNailgun(); // Activate the usage of Nailgun
 
        app.get('/png/:uml', function(req, res) {
          res.set('Content-Type', 'image/png');
          logger.log("info", 'Get Plantuml');
         
          //var decode = plantuml.decode(req.params.uml);
          var readFile = fs.createReadStream("./generated_uml_"+req.params.uml+"/"+req.params.uml+"/docs/"+req.params.uml+".plantuml");
          var gen = plantuml.generate({format: 'png'});
         
          readStream.on('error', function(err) {
            res.end(err);
          });

          //decode.out.pipe(gen.in);
          readFile.pipe(gen.in);
          gen.out.pipe(res);
        });
         
        app.get('/svg/components/:uml', function(req, res) {
          res.set('Content-Type', 'image/svg+xml');
         
          //var decode = plantuml.decode(req.params.uml);
          var readFile = fs.createReadStream("./generated_uml_"+req.params.uml+"/"+req.params.uml+"/docs/"+req.params.uml+".plantuml");
          var gen = plantuml.generate({format: 'svg'});
         
          readStream.on('error', function(err) {
            res.end(err);
          });

          //decode.out.pipe(gen.in);
          readFile.pipe(gen.in);
          gen.out.pipe(res);
        });

        app.get('/svg/class/:uml', function(req, res) {
          res.set('Content-Type', 'image/svg+xml');
         
          //var decode = plantuml.decode(req.params.uml);
          var readFile = fs.createReadStream("./generated_uml_"+req.params.uml+"/"+req.params.uml+"/docs/"+req.params.uml+"_class.plantuml");
          var gen = plantuml.generate({format: 'svg'});
          
          readStream.on('error', function(err) {
            res.end(err);
          });

          //decode.out.pipe(gen.in);
          readFile.pipe(gen.in);
          gen.out.pipe(res);
        });

        app.get('/svg/datatypes/:uml', function(req, res) {
          res.set('Content-Type', 'image/svg+xml');
         
          //var decode = plantuml.decode(req.params.uml);
          var readFile = fs.createReadStream("./generated_uml_"+req.params.uml+"/"+req.params.uml+"/docs/"+req.params.uml+"_datatypes.plantuml");
          var gen = plantuml.generate({format: 'svg'});
         
          readStream.on('error', function(err) {
            res.end(err);
          });

          //decode.out.pipe(gen.in);
          readFile.pipe(gen.in);
          gen.out.pipe(res);
        });
        
        logger.log("info", 'PlantUML diagram generator started on port: '+8081);
        app.listen(8081);
    };

    return that;
}());

module.exports = plantuml_generator;
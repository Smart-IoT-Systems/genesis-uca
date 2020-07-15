var mm = require('../metamodel/allinone.js');
const fs = require('fs');
const readline = require('readline');
const insertLine = require('insert-line');

/***********************************/
/* Specific SMOOL KP with security */
/***********************************/
var smoolkp = function (spec) {
    var that = mm.software_node(spec);
    that._type += "/thingml/smoolkp";
    that.name = spec.name || "SmoolKP";
    that.file = spec.file || "/Users/ferrynico/Documents/Code/GeneSIS-gitlab/docker-node-red-thingml/doc/examples/LightSensorApp/LightSensorApp.thingml",
    that.security_policy = spec.security_policy || [["", ""]];
    that.target_language = spec.target_language || "java"
    that.config_name = spec.config_name || that.name;
    that.security_checker = spec.security_checker || "";

    // This function is called before the resources are used
    that._configure = async function () {



        const rl = readline.createInterface({
            input: fs.createReadStream(that.file),
            output: process.stdout
        });
        for await (const line of rl) {
            if (line.startsWith("@src")) {
                var path_java_src = line.split('"')[1];
                var onlyPath = require('path').dirname(that.file);
                var path_checker = onlyPath + '/' + path_java_src + "/org/smool/security/SecurityChecker.java";
                break;
            }
        }

        if(that.security_checker === ""){
            const rl2 = readline.createInterface({
                input: fs.createReadStream(path_checker),
                output: process.stdout
            });
            var pos_reader = 0;
    
            var content_for_insertion = '';
            that.security_policy.forEach(policies => {
                content_for_insertion += 'policies.put("' + policies[0] + '", "' + policies[1] + '");\n';
            });
    
            for await (const line2 of rl2) {
                pos_reader++;
                if (line2.indexOf("public SecurityChecker()") > 0) {
                    insertLine(path_checker).contentSync(content_for_insertion).at(pos_reader + 1);
                    break;
                }
            }    
        }else{
            var contents = fs.readFileSync(that.security_checker);
            fs.writeFileSync(path_checker, contents);
        }
        
        return content_for_insertion;
    };

    return that;
};

module.exports = smoolkp;
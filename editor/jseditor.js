var container = document.getElementById('jsoneditor');

options = {
    mode: 'tree',
    modes: ['code', 'form', 'text', 'tree', 'view'], // allowed modes
    name: "jsonContent",
    onError: function (err) {
        alert(err.toString());
    }
};

var editor = new JSONEditor(container, options);

document.getElementById('saveJSON').onclick = function () {
    var tmp_edited = editor.get();
    var edited = deployment_model(tmp_edited);
    edited.components = tmp_edited.components;
    edited.revive_links(tmp_edited.links);

    if (edited.is_valid()) {
        var r = compare(dm, edited);
        dm = edited;
        alertMessage("success", "Model updated!", 3000);
    } else {
        //TODO: we need to provide more relevant info
        alertMessage("error", "Model is not valid!", 3000);
    }
};

function compare(src_dm, target_dm) {

    //Added Hosts and components
    var target_comps = target_dm.components;
    for (var i in target_comps) {
        var tmp_node = src_dm.find_node_named(target_comps[i].name);
        if (tmp_node === undefined) {
            var fac = graph_factory(target_comps[i].name);
            var node = fac.create_node(target_comps[i]._type);
            console.log("added: " + JSON.stringify(node));
            cy.add(node);
        }
    }

    //Removed hosts and components
    var comps = src_dm.components;
    for (var i in comps) {
        var tmp_host = target_dm.find_node_named(comps[i].name);
        if (tmp_host === undefined) {
            console.log("Here we go");
            //if removed component is hosting components and formerly hosted components are still in model we should move them
            var tmp_h = src_dm.get_hosted(comps[i].name);
            tmp_h.forEach(function(e){
                var _tmp=target_dm.find_node_named(e.name);
                if(_tmp !== undefined){
                    cy.$('#' + e.name).move({
                        parent: _tmp.id_host
                    });
                }
                if(e.id_host === null){
                    cy.$('#' + e.name).move({
                        parent: null
                    });
                }
            });

            //Then we remove the hosts
            cy.remove('#' + comps[i].name);
        }
    }

    //Added links
    var target_links = target_dm.links;
    for (var i in target_links) {
        var tmp_link = src_dm.find_link_named(target_links[i].name);
        if (tmp_link === undefined) {
            tmp_link.forEach(function (elem) {
                //add to graph
                var edge = {
                    group: "edges",
                    data: {
                        id: elem.name,
                        source: elem.src,
                        target: elem.target
                    }
                };
                if (elem.isControl = true) {
                    edge.classes = 'control';
                }
                cy.add(edge);
            });
        }
    }

    //Removed or modified links
    var links = src_dm.links;
    for (var i in links) {
        var tmp_link = target_dm.find_link_named(links[i].name);
        if (tmp_link === undefined) {
            cy.remove('#' + links[i].name);
        } else {
            //check for modified links: (1) control 
            console.log(JSON.stringify(tmp_link));
            if (tmp_link.isControl && !links[i].isControl) {
                cy.$('#' + tmp_link.name).classes = 'control';
            } else {
                if (!tmp_link.isControl && links[i].isControl) {
                    cy.$('#' + tmp_link.name).removeClass('control');
                }
            }
            //and (2) src and targets
            if (tmp_link.src !== links[i].src || tmp_link.target !== links[i].target) {
                cy.remove('#' + links[i].name);
                var edge_modified = {
                    group: "edges",
                    data: {
                        id: tmp_link.name,
                        source: tmp_link.src,
                        target: tmp_link.target
                    }
                };
                cy.add(edge_modified);
            }
        }
    }

}
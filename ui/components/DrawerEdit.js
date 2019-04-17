import React from "react";
import 'antd/dist/antd.css';
import {
    Drawer, Button, Form, Row, Col,
} from 'antd';
import EditForm from './EditForm.js';

const EditionForm = Form.create({ name: 'coordinated' })(EditForm);

class DrawerEdit extends React.Component {
    
    constructor(){
      super();
      this.state = { 
        visible: false,
        elem: null,
        elem_model: null,
      };
      window.DrawerEdit=this;
    }

    setElem = (e) => {
      this.setState({
        elem: e,
      });
    }

    showDrawer = (e, em) => {
      this.setState({
        visible: true,
        elem: e,
        elem_model: em,
      });
    };
  
    onClose = () => {
      this.setState({
        visible: false,
      });
    };

    onDelete = () => {
      if (this.state.elem.src) {//Check if it is a link
        cy.remove(this.state.elem); //remove from the display
        window.SiderDemo.getDM().remove_link(this.state.elem_model); //remove from the model
      } else {
        cy.remove(this.state.elem); //remove from the display
        window.SiderDemo.getDM().remove_component(this.state.elem_model); //remove from the model together with associated links
      }
      this.setState({
        visible: false,
      });
    }

    handleGoTo = () => {
      var port = window.FormEdit.state.element.port;
      var host_id = window.FormEdit.state.element.id_host;
      var h = window.SiderDemo.getDM().find_node_named(host_id);
      var win = window.open('http://' + h.ip + ':' + port, '_blank');
      win.focus();
    }
  
    onSave = (e) => {
      if (this.state.elem.src) {//Check if it is a link
          var target_link=this.state.elem;
          if (!target_link.isControl) {
            cy.$('#' + target_link.id()).removeClass('control');
          }
        } else {
          var target_node=this.state.elem;

          //First, we update the display (in the graph and its style to update the label display)
          if (target_node.id() !== window.FormEdit.state.element.name) {
              //In cytoscape ids are immutable so if name has changed we need to create a new node
              var tmp = target_node.json();
              tmp.data.id = window.FormEdit.state.element.name;
              tmp.data.parent = window.FormEdit.state.element.id_host;// We also update the host in case we also changed it
              cy.add(tmp);
              //If the node is hosting other nodes
              var tmp_hosted = target_node.children();
              if (tmp_hosted !== undefined) {
                  tmp_hosted.forEach(function (elem) {
                      elem.move({
                          parent: window.FormEdit.state.element.name
                      });
                      var component_tmp = dm.find_node_named(elem.id());
                      component_tmp.id_host = window.FormEdit.state.element.name;
                  });
              }
              //Then we check the links
              target_node.connectedEdges().forEach(function(edg){
                if(edg.data().target !== window.FormEdit.state.element.name
                  && edg.data().target === target_node.id()){
                  edg.move({
                    target: window.FormEdit.state.element.name
                  });
                }
                if(edg.data().source !== window.FormEdit.state.element.name
                  && edg.data().source === target_node.id()){
                  edg.move({
                    source: window.FormEdit.state.element.name
                  });
                }
              });
              //Finally we remove the all component
              cy.remove("#" + target_node.id());
          }else{
            //If the node was hosted and host has changed
            if(target_node.parent() !== window.FormEdit.state.element.id_host){
              target_node.move({
                parent: window.FormEdit.state.element.id_host
              });
            }
          }
        }
      //Finally, we update the model
      for (var prop in this.state.elem_model) {
        this.state.elem_model[prop]=window.FormEdit.state.element[prop];
      }
      this.onClose();
    }

    render() {
      return (
        <div>
          <Drawer title="Edit" width={720} onClose={this.onClose} visible={this.state.visible} destroyOnClose={true}>
            <EditionForm />
            <div style={{ position: 'absolute', left: 0, bottom: 0, width: '100%', borderTop: '1px solid #e9e9e9', padding: '10px 16px', background: '#fff', }} > 
              <Row>
                <Col span={8}><Button icon="delete" onClick={this.onDelete} type="danger">
                    Delete
                  </Button></Col>
                <Col span={8} offset={8}>
                  <div style={{ textAlign: 'right', }}>
                    <Button icon="save" onClick={this.onSave} style={{ marginRight: 8 }}  type="primary">
                      Save
                    </Button>
                    <Button icon="right-circle" onClick={this.handleGoTo}>
                      Go To!
                    </Button>
                  </div>
                </Col>
              </Row>
            </div>
          </Drawer>
        </div>
      );
    }
  }
  
  export default DrawerEdit;
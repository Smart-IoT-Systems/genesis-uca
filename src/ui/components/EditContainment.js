import React from "react";
import 'antd/dist/antd.css';
import { Form, Input, Icon, Tooltip, Cascader, Drawer, Row, Col, Button } from 'antd';

class EditContainment extends React.Component {

    constructor(){
        super();
        this.state = { 
          visible: false,
          name: "",
          tgt: null,
          elem_model: null,
          elem: null,
        };
        window.EditContainment=this;
    }

    showDrawer = (e,em) => {
        var d_m=window.SiderDemo.getDM();
        var id = d_m.generate_port_id(em, em.required_execution_port);
        var p=d_m.find_containment_of_required_port(id);
        this.setState({
          visible: true,
          elem_model: em,
          name: p.name,
          elem: e,
        });
    }

    handleChangeName = (event) => {
        event.preventDefault();
        this.setState({
            name: event.target.value,
        });
    }

    onChangeTarget = (value) => {
        this.setState({
            tgt: value,
        });
    }

    onDelete = () => {
        //First we update the model
        var d_m=window.SiderDemo.getDM();
        var id = d_m.generate_port_id(this.state.elem_model, this.state.elem_model.required_execution_port);
        var c=d_m.find_containment_of_required_port(id);
        d_m.remove_containment(c);

        //Then we update the graph
        cy.$('#' + this.state.elem.name).move({
            parent: null
        });

        this.setState({
            visible: false,
        });
    }

    onSave = (e) => {
        var selectedTarget=this.state.tgt+"";
        var selectedTargetComp = selectedTarget.split(',')[0];
        var selectedTargetPort = selectedTarget.split(',')[1];

        var target_node=this.state.elem;
        var d_m=window.SiderDemo.getDM();

        //First we update the graph
        var h = d_m.find_host(this.state.elem_model);
        if(target_node.parent() !== h.name){
            target_node.move({
                parent: selectedTargetComp
            });
        }

        //Then we update the model
        var id = d_m.generate_port_id(this.state.elem_model, this.state.elem_model.required_execution_port);
        var c=d_m.find_containment_of_required_port(id);
        c.target = '/'+selectedTargetComp+'/'+selectedTargetPort;

        window.SiderDemo.openNotificationWithIcon('success', 'Model updated!', 'The deployment model has been successfully updated!');
        this.setState({
            visible: false,
        });
    }

    onClose = () => {
        this.setState({
          visible: false,
        });
    };

    render() {
        var d_m=window.SiderDemo.getDM();
        var optionsHosts=[];
        d_m.components.forEach(elt => {
            if(elt.provided_execution_port.length > 0){
                var o={ value: elt.name, label: elt.name};
                o.children = [];
                elt.provided_execution_port.forEach(port => {
                    var cp = {value: port.name, label: port.name};
                    o.children.push(cp);
                });
                optionsHosts.push(o);
            }
        });

        return (
            <Drawer title="Edit Containment" width={720} onClose={this.onClose} visible={this.state.visible} destroyOnClose={true}>
                <Form>
                    <Form.Item label="Name:" key="1">
                            <Input name="name" defaultValue={this.state.name} onChange={this.handleChangeName} prefix={<Icon type="user" style={{ color: 'rgba(0,0,0,.25)' }} />} suffix={<Tooltip title="Must be uniq"><Icon type="info-circle" style={{ color: 'rgba(0,0,0,.45)' }} /></Tooltip>} placeholder="Name" />
                    </Form.Item>
                    <Form.Item label="Hosted On:" key="2">
                            <Cascader options={optionsHosts} onChange={this.onChangeTarget} placeholder="Please select" />
                    </Form.Item>
                    <br/>
                </Form>
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
                  </div>
                </Col>
              </Row>
            </div>
            </Drawer>
        );
    }

}

export default EditContainment;
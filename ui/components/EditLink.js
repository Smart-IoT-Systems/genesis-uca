import React from "react";
import 'antd/dist/antd.css';
import { Form, Input, Icon, Tooltip, Cascader, Drawer, Row, Col, Button, Checkbox } from 'antd';

const CheckboxGroup = Checkbox.Group;
const plainOptions = ['isControl', 'isMandatory', 'isDeployer'];
const defaultCheckedList = [];

class EditLink extends React.Component {

    constructor(){
        super();
        this.state = { 
          name: "",
          visible: false,
          checkedList: defaultCheckedList,
          src: null,
          tgt: null,
          elem_model: null,
          elem: null,
        };
        window.EditLink=this;
    }

    showDrawer = (e,em) => { //TODO: init checklist
        var d_m=window.SiderDemo.getDM();
        var comp_target=d_m.get_comp_name_from_port_id(em.target);
        var port_target=d_m.get_port_name_from_port_id(em.target);
        var comp_src=d_m.get_comp_name_from_port_id(em.src);
        var port_src=d_m.get_port_name_from_port_id(em.src);
        var source = [comp_src,port_src];
        var t=[comp_target,port_target];

        var check=[]
        if (em.isDeployer) {
            check.push('isDeployer');
        }
        if (em.isMandatory) {
            check.push('isMandatory');
        }
        if (em.isControl) {
            check.push('isControl');
        }

        this.setState({
          visible: true,
          elem_model: em,
          name: em.name,
          elem: e,
          src:source,
          tgt:t,
          checkedList: check,
        });
    }

    onClose = () => {
        this.setState({
          visible: false,
        });
    }

    handleChangeName = (event) => {
        event.preventDefault();
        this.setState({
            name: event.target.value,
        });
    }

    onChangeSource = (value) => {
        this.setState({
            src: value,
        });
    }

    onChangeTarget = (value) => {
        this.setState({
            tgt: value,
        });
    }

    onChangeCheckList = (check) => {
        this.setState({
            checkedList: check,
        });
    }

    onDelete = () => {
        cy.remove(this.state.elem); //remove from the display
        window.SiderDemo.getDM().remove_link(this.state.elem_model); //remove from the model
        this.setState({
            visible: false,
        });
    }

    onSave = () => {
        
        var selectedSrc=this.state.src+"";
        var selectedTarget=this.state.tgt+"";
        var selectedSrcComp = selectedSrc.split(',')[0];
        var selectedSrcPort = selectedSrc.split(',')[1];
        var selectedTargetComp = selectedTarget.split(',')[0];
        var selectedTargetPort = selectedTarget.split(',')[1];


        //First we update the graph
        var target_link=this.state.elem;
        if (!this.state.elem_model.isControl) {
            cy.$('#' + target_link.id()).removeClass('control');
        }else{
            cy.$('#' + target_link.name).classes = 'control';
        }

        if (target_link.data().source !== selectedSrcComp || target_link.data().target !== selectedTargetComp) {
            cy.remove('#' + target_link.data().name);
            var edge_modified = {
                group: "edges",
                data: {
                    id: target_link.data().name,
                    source: selectedSrcComp,
                    target: selectedTargetComp,
                }
            };

            if (this.state.elem_model.isControl) {
                edge_modified.classes = 'control';
            }

            cy.add(edge_modified);
        }

        //Then we update the model
        this.state.elem_model.src='/'+selectedSrcComp+'/'+selectedSrcPort;
        this.state.elem_model.target='/'+selectedTargetComp+'/'+selectedTargetPort;
        this.state.elem_model.name=this.state.name;

        this.state.elem_model.isDeployer = false;
        this.state.elem_model.isMandatory = false;
        this.state.elem_model.isControl = false;

        this.state.checkedList.forEach(e => {
            if (e === 'isDeployer') {
                this.state.elem_model.isDeployer = true;
            }
            if (e === 'isMandatory') {
                this.state.elem_model.isMandatory = true;
            }
            if (e === 'isControl') {
                this.state.elem_model.isControl = true;
            }
        });

        window.SiderDemo.openNotificationWithIcon('success', 'Model updated!', 'The deployment model has been successfully updated!');
        this.setState({
            visible: false,
        });
    }

    render() {
        var optionsTgt=[];
        var optionsSrc=[];
        var d_m=window.SiderDemo.getDM();
        d_m.get_all_can_be_hosted().forEach(element => {
            var optSrc={ value: element.name, label: element.name};
            optSrc.children = [];
            element.provided_communication_port.forEach(port => {
                var c = {value: port.name, label: port.name};
                optSrc.children.push(c);
            });
            optionsSrc.push(optSrc);

            var optTgt={ value: element.name, label: element.name};
            optTgt.children = [];
            element.required_communication_port.forEach(port => {
                var ct = {value: port.name, label: port.name};
                optTgt.children.push(ct);
            });
            optionsTgt.push(optTgt);
        });

        return (
            <Drawer title="Edit Containment" width={720} onClose={this.onClose} visible={this.state.visible} destroyOnClose={true}>
                <Form>
                    <Form.Item label="Name:" key="1">
                            <Input name="name" defaultValue={this.state.name} onChange={this.handleChangeName} prefix={<Icon type="user" style={{ color: 'rgba(0,0,0,.25)' }} />} suffix={<Tooltip title="Must be uniq"><Icon type="info-circle" style={{ color: 'rgba(0,0,0,.45)' }} /></Tooltip>} placeholder="Name" />
                    </Form.Item>
                    <Form.Item label="Source:" key="2">
                        <Cascader expandTrigger="hover" defaultValue={this.state.src} options={optionsSrc} onChange={this.onChangeSource} placeholder="Please select" />
                    </Form.Item>
                    <Form.Item label="Target:" key="3">
                        <Cascader expandTrigger="hover" defaultValue={this.state.tgt} options={optionsTgt} onChange={this.onChangeTarget} placeholder="Please select" />
                    </Form.Item>
                    <Form.Item label="Properties:" key="4">
                        <CheckboxGroup options={plainOptions} value={this.state.checkedList} onChange={this.onChangeCheckList} />
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
export default EditLink;
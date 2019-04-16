import React from "react";
import 'antd/dist/antd.css';
import { Modal, Form, Checkbox, Input, Icon, Tooltip, Cascader } from 'antd';

class AddContainmentModal extends React.Component {

    constructor(){
        super();
        this.state = { 
          visible: false,
          name: "",
          src: null,
          tgt: null,
        };
        window.AddContainmentModal=this;
    }

    showModal = (e) => {
        this.setState({
          visible: true,
        });
    };

    onClose = () => {
        this.setState({
          visible: false,
        });
    };

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

    handleOk = (e) => {
        if(this.state.src !== null && this.state.tgt !== null){
            var selectedSrc=this.state.src+"";
            var selectedTarget=this.state.tgt+"";
        
            //update graph
            var node = cy.getElementById(selectedSrc);
            node.move({
                parent: selectedTarget
            });
        
            //update model
            var comp = window.SiderDemo.getDM().find_node_named(selectedSrc);
            comp.id_host = selectedTarget;
        }

        this.setState({
            visible: false,
        });
    }

    render() {

        var optionsComponents=[];
        var d_m=window.SiderDemo.getDM();
        d_m.get_all_hosted().forEach(element => {
            var opt={ value: element.name, label: element.name};
            optionsComponents.push(opt);
        });

        var optionsHosts=[];
        d_m.get_all_hosts().forEach(elt => {
            var o={ value: elt.name, label: elt.name};
            optionsHosts.push(o);
        });

        return (
            <div>
            <Modal
                title="Add Containment"
                visible={this.state.visible}
                onCancel={this.onClose}
                onOk={this.handleOk}
            >
                <Form>
                    <Form.Item label="Name:" key="1">
                        <Input name="name" onChange={this.handleChangeName} prefix={<Icon type="user" style={{ color: 'rgba(0,0,0,.25)' }} />} suffix={<Tooltip title="Must be uniq"><Icon type="info-circle" style={{ color: 'rgba(0,0,0,.45)' }} /></Tooltip>} placeholder="Name" />
                    </Form.Item>
                    <Form.Item label="Component:" key="2">
                        <Cascader options={optionsComponents} onChange={this.onChangeSource} placeholder="Please select" />
                    </Form.Item>
                    <Form.Item label="Hosted On:" key="3">
                        <Cascader options={optionsHosts} onChange={this.onChangeTarget} placeholder="Please select" />
                    </Form.Item>
                </Form>   
            </Modal>
            </div>
        );
    }
}
export default AddContainmentModal;
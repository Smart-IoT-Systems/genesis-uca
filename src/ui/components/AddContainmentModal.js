import React from "react";
import 'antd/dist/antd.css';
import { Modal, Form, Input, Icon, Tooltip, Cascader } from 'antd';

class CAddContainmentModal extends React.Component {

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

    showModal = () => {
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
        this.props.form.validateFields((err) => {
            if (!err) {
                if(this.state.src !== null && this.state.tgt !== null){
                    var selectedSrc=this.state.src+"";
                    var selectedTarget=this.state.tgt+"";
                    var selectedSrcComp = selectedSrc.split(',')[0];
                    var selectedSrcPort = selectedSrc.split(',')[1];
                    var selectedTargetComp = selectedTarget.split(',')[0];
                    var selectedTargetPort = selectedTarget.split(',')[1];
                
                    //update graph
                    var node = cy.getElementById(selectedSrcComp);
                    var parent = cy.getElementById(selectedTargetComp);
                    parent.addClass('host');
                    node.move({
                        parent: selectedTargetComp
                    });
                
                    //update model
                    var m_m=window.SiderDemo.getMM();
                    var h = m_m.hosting({});
                    h.name = this.state.name;
                    h.target = '/'+selectedSrcComp+'/'+selectedSrcPort;
                    h.src = '/'+selectedTargetComp+'/'+selectedTargetPort;

                    window.SiderDemo.getDM().containments.push(h);
                }

                this.setState({
                    visible: false,
                });
            }
        });
    }

    render() {

        const { getFieldDecorator } = this.props.form;

        var optionsComponents=[];
        var d_m=window.SiderDemo.getDM();
        d_m.get_all_internals().forEach(element => {
            var opt={ value: element.name, label: element.name};
            opt.children = [];
            var port = element.required_execution_port;
            var c = {value: port.name, label: port.name};
            opt.children.push(c);
            optionsComponents.push(opt);
        });

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
            <div>
            <Modal
                title="Add Containment"
                visible={this.state.visible}
                onCancel={this.onClose}
                onOk={this.handleOk}
                destroyOnClose={true}
            >
                <Form>
                    <Form.Item label="Name:" key="1">

                    {getFieldDecorator('Name', {
                        rules: [{
                        required: true,
                        message: 'Please input a name',
                        }],
                    })(
                        <Input onChange={this.handleChangeName} prefix={<Icon type="user" style={{ color: 'rgba(0,0,0,.25)' }} />} suffix={<Tooltip title="Must be uniq"><Icon type="info-circle" style={{ color: 'rgba(0,0,0,.45)' }} /></Tooltip>} placeholder="Name" />
                    )}
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

const AddContainmentModal = Form.create()(CAddContainmentModal);

export default AddContainmentModal;
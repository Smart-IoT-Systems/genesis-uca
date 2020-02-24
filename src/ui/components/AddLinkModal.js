import React from "react";
import 'antd/dist/antd.css';
import { Modal, Form, Checkbox, Input, Icon, Tooltip, Cascader } from 'antd';


const CheckboxGroup = Checkbox.Group;
const plainOptions = ['isControl', 'isMandatory', 'isDeployer'];
const defaultCheckedList = [];

class CAddLinkModal extends React.Component {

    constructor(){
        super();
        this.state = { 
          name: "",
          visible: false,
          checkedList: defaultCheckedList,
          src: null,
          tgt: null,
        };
        window.AddLinkModal=this;
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

    onChangeCheckList = (checkedList) => {
        this.setState({
            checkedList,
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
        
                    //add to graph
                    var edge = {
                        group: "edges",
                        data: {
                            id: this.state.name,
                            source: selectedSrcComp,
                            target: selectedTargetComp
                        }
                    };
        
                    var m_m=window.SiderDemo.getMM();
                    var l = m_m.link({});
                    l.name = this.state.name;
                    l.src = '/'+selectedSrcComp+'/'+selectedSrcPort;
                    l.target = '/'+selectedTargetComp+'/'+selectedTargetPort;
        
                    this.state.checkedList.forEach(e => {
                        if (e === 'isDeployer') {
                            edge.classes = 'isdeployer';
                            l.isDeployer = true;
                        }
                        if (e === 'isMandatory') {
                            l.isMandatory = true;
                        }
                        if (e === 'isControl') {
                            edge.classes = 'control';
                            l.isControl = true;
                        } else {
                            l.isControl = false;
                        }
                    });
        
                    cy.add(edge);
                    window.SiderDemo.getDM().links.push(l);
                }
                this.setState({
                    visible: false,
                });
            }
        });        
    }

    render() {

        const { getFieldDecorator } = this.props.form;

        var optionsTgt=[];
        var optionsSrc=[];
        var d_m=window.SiderDemo.getDM();
        d_m.get_all_software_components().forEach(element => {
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
            <div>
            <Modal
                title="Add Communication"
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
                    <Form.Item label="Source:" key="2">
                        <Cascader expandTrigger="hover" options={optionsSrc} onChange={this.onChangeSource} placeholder="Please select" />
                    </Form.Item>
                    <Form.Item label="Target:" key="3">
                        <Cascader expandTrigger="hover" options={optionsTgt} onChange={this.onChangeTarget} placeholder="Please select" />
                    </Form.Item>
                    <Form.Item label="Properties:" key="4">
                        <CheckboxGroup options={plainOptions} value={this.state.checkedList} onChange={this.onChangeCheckList} />
                    </Form.Item>
                </Form>   
            </Modal>
            </div>
        );
    }


}

const AddLinkModal = Form.create()(CAddLinkModal);

export default AddLinkModal;
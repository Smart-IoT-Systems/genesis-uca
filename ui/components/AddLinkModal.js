import React from "react";
import 'antd/dist/antd.css';
import { Modal, Form, Checkbox, Input, Icon, Tooltip, Cascader } from 'antd';


const CheckboxGroup = Checkbox.Group;
const plainOptions = ['isControl', 'isMandatory', 'isDeployer'];
const defaultCheckedList = [];

class AddLinkModal extends React.Component {

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

    onChangeCheckList = (checkedList) => {
        this.setState({
            checkedList,
        });
    }

    handleOk = (e) => {
        if(this.state.src !== null && this.state.tgt !== null){
            var selectedSrc=this.state.src+"";
            var selectedTarget=this.state.tgt+"";

            //add to graph
            var edge = {
                group: "edges",
                data: {
                    id: this.state.name,
                    source: selectedSrc,
                    target: selectedTarget
                }
            };

            var m_m=window.SiderDemo.getMM();
            var l = m_m.link({});
            l.name = name;
            l.src = selectedSrc;
            l.target = selectedTarget;

            this.state.checkedList.forEach(e => {
                if (e === '#isDeployer') {
                    l.isDeployer = true;
                }
                if (e === '#isMandatory') {
                    l.isMandatory = true;
                }
                if (e === '#isController') {
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

    render() {

        var options=[];
        var d_m=window.SiderDemo.getDM();
        d_m.get_all_hosted().forEach(element => {
            var opt={ value: element.name, label: element.name};
            options.push(opt);
        });

        return (
            <div>
            <Modal
                title="Add Communication"
                visible={this.state.visible}
                onCancel={this.onClose}
                onOk={this.handleOk}
            >
                <Form>
                    <Form.Item label="Name:" key="1">
                        <Input name="name" onChange={this.handleChangeName} prefix={<Icon type="user" style={{ color: 'rgba(0,0,0,.25)' }} />} suffix={<Tooltip title="Must be uniq"><Icon type="info-circle" style={{ color: 'rgba(0,0,0,.45)' }} /></Tooltip>} placeholder="Name" />
                    </Form.Item>
                    <Form.Item label="Source:" key="2">
                        <Cascader options={options} onChange={this.onChangeSource} placeholder="Please select" />
                    </Form.Item>
                    <Form.Item label="Target:" key="3">
                        <Cascader options={options} onChange={this.onChangeTarget} placeholder="Please select" />
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

export default AddLinkModal;
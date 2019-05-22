import React from "react";
import 'antd/dist/antd.css';
import { Modal, Form, Select, Input } from 'antd';

const { TextArea } = Input;

class RestCallModal extends React.Component {

    constructor(){
        super();
        this.state = { 
            visible: false,
            elem_name: {},
          };
        window.RestCallModal=this;
    }

    showModal = (e) => {
        this.setState({
          visible: true,
          elem_name: e,
        });
    };

    handleOk = () => {
        
    }

    onClose = () => {
        this.setState({
          visible: false,
        });
    };

    render() {
        return (
            <div>
                <Modal
                title="Perform a REST call"
                visible={this.state.visible}
                onCancel={this.onClose}
                onOk={this.handleOk}
                destroyOnClose={true}
            >
                <Form>
                    <Form.Item label="Method">
                        <Select defaultValue="1">
                            <Option value="1">GET</Option>
                            <Option value="2">POST</Option>
                            <Option value="3">PUT</Option>
                        </Select>
                    </Form.Item>
                    <Form.Item label="URI">
                        <Input placeholder="http://" id="uri" />
                    </Form.Item>
                    <Form.Item label="Content-type">
                        <Input placeholder="application/json" id="content-type" />
                    </Form.Item>
                    <Form.Item label="Body">
                        <TextArea id="body" />
                    </Form.Item>
                </Form>
            </Modal>
            </div>
        );
    }


}

export default RestCallModal;
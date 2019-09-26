import React from "react";
import 'antd/dist/antd.css';
import { Modal, Form } from 'antd';

import EditForm from './EditForm.js';

class AddModal extends React.Component {

    constructor(){
        super();
        this.state = { 
          visible: false,
          elem: null,
        };
        window.AddModal=this;
    }

    showModal = (e) => {
        this.setState({
          visible: true,
          elem: e,
        });
    };

    handleOk = (e) => {
        var fac = graph_factory(window.FormEdit.state.element.name);
        var node = fac.create_node(window.FormEdit.state.element._type);
        cy.add(node);
        for (var prop in this.state.elem) {
            if(window.FormEdit.state.element[prop]){
                if(typeof this.state.elem[prop] === 'object' && typeof window.FormEdit.state.element[prop] !== 'object'){
                    this.state.elem[prop]=JSON.parse(window.FormEdit.state.element[prop]);
                }else{
                    this.state.elem[prop]=window.FormEdit.state.element[prop];
                }
            }
        }

        window.SiderDemo.getDM().components.push(this.state.elem);
        window.SiderDemo.openNotificationWithIcon("success",'Model updated!', 'The deployment model has been successfully updated!');
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

        return (
            <div>
            <Modal
                title="Add Component"
                visible={this.state.visible}
                onCancel={this.onClose}
                onOk={this.handleOk}
                destroyOnClose={true}
            >
                <EditForm name="Addition" elem={this.state.elem} />
            </Modal>
            </div>
        );
    }

}

export default AddModal;
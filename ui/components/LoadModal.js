import React from "react";
import 'antd/dist/antd.css';
import { Modal, Upload, Icon, message } from 'antd';



class LoadModal extends React.Component {

  constructor(){
    super();
    this.state = { 
      visible: false,
      uploading: false,
      fileList: [],
    };
    window.LoadModal=this;
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

  handleOk = () => {
    var fd=this.state.fileList[0];
    var fr;
    fr = new FileReader();
    fr.onload = receivedText;
    fr.readAsText(fd);

    function receivedText() { //We ask the server because he is the one with all the plugins ...
      try{
        var data = JSON.parse(fr.result);
      }catch(err){
        window.SiderDemo.openNotificationWithIcon('error', 'Invalid JSON', JSON.stringify(err));
        return;
      }
      var m_m=window.SiderDemo.getMM();
      var dm = m_m.deployment_model(data.dm);
      dm.components = data.dm.components;
      dm.revive_links(data.dm.links);
      dm.revive_containments(data.dm.containments);
      var tab_errors = dm.is_valid_with_errors();
      if(tab_errors.length > 0){
        window.SiderDemo.openNotificationWithIcon('error', 'Invalid Model', tab_erros[0]);
        return;
      }
      window.SiderDemo.setDM(dm);
      cy.json(data.graph);
    }

    this.setState({
      visible: false,
      fileList: [],
    });
  }

  render() {

    const Dragger = Upload.Dragger;

    const props = {
      name: 'file',
      multiple: false,
      onRemove: (file) => {
        this.setState((state) => {
          const index = state.fileList.indexOf(file);
          const newFileList = state.fileList.slice();
          newFileList.splice(index, 1);
          return {
            fileList: newFileList,
          };
        });
      },
      beforeUpload: (file) => {
        this.setState(state => ({
          fileList: [...state.fileList, file],
        }));
        return false;
      }
    };

    return (
      <div>
        <Modal
          title="Load Model"
          visible={this.state.visible}
          onOk={this.handleOk}
          onCancel={this.onClose}
          destroyOnClose={true}
        >
          <Dragger {...props}>
            <p className="ant-upload-drag-icon">
              <Icon type="inbox" />
            </p>
            <p className="ant-upload-text">Click or drag Model to this area to upload</p>
            <p className="ant-upload-hint">Support for a single upload. </p>
          </Dragger>
        </Modal>
      </div>
    );
  }
}

export default LoadModal;
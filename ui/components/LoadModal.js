import React from "react";
import 'antd/dist/antd.css';
import { Modal, Upload, Icon, message } from 'antd';



class LoadModal extends React.Component {

  state = {
    fileList: [],
    uploading: false,
    visible: this.props.visible,
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
          visible={this.props.visible}
          onOk={()=>this.props.handleOk(this.state.fileList)}
          onCancel={this.props.handleCancel}
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
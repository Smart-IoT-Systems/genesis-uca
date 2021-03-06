import React from "react";
import 'antd/dist/antd.css';
import {
    Checkbox, Form, Icon, Input, Tooltip, Row, Col, Collapse
} from 'antd';

const CheckboxGroup = Checkbox.Group;
const Panel = Collapse.Panel;

const { TextArea } = Input;
const plainOptions = ['needDeployer'];

let id=0;

class FormEdit extends React.Component {
    
    constructor(props){
      super();
      var tmp_elem = JSON.parse(JSON.stringify(props.elem));
      var check = [];
      if (tmp_elem.needDeployer) {
        check.push('needDeployer');
      }
      this.state = { 
        element: tmp_elem,
        checkedList: check,
      };
      window.FormEdit=this;
      this.handleChange = this.handleChange.bind(this);
    }
  
    get_all_properties(elem) {
        var props = [];
        for (var prop in elem) {
            if (typeof elem[prop] != 'function') {
                props.push(prop);
            }
        }
        return props;
    }

    handleChange(event){
        event.preventDefault();
        var newElem=this.state.element;
        if(event.target.type === "checkbox"){
            newElem[event.target.name]=event.target.checked;
        }else{
            newElem[event.target.name]=event.target.value;
        }
    }

    onChangeCheckList = (check) => {
        this.setState({
            checkedList: check,
        });

        this.state.element.needDeployer = false;
        check.forEach(e => {
            if (e === 'needDeployer') {
                this.state.element.needDeployer = true;

            }
        });
    }
  
    render() {  

        var elem=this.state.element;
        var result=[];
        var props = this.get_all_properties(elem);

        result.push(
            <Row key={id++} gutter={16}>
                <Col span={12}>
                    <Form.Item label="Name:" key={id++}>
                        <Input onChange={this.handleChange} defaultValue={elem["name"]} name="name" prefix={<Icon type="user" style={{ color: 'rgba(0,0,0,.25)' }} />} suffix={<Tooltip title="Must be uniq"><Icon type="info-circle" style={{ color: 'rgba(0,0,0,.45)' }} /></Tooltip>} placeholder="Name" />
                    </Form.Item>
                </Col>
                <Col span={12}>
                    <Form.Item label="_type:" key={id++}>
                        <Input onChange={this.handleChange} defaultValue={elem["_type"]} disabled={true} prefix={<Icon type="tag" style={{ color: 'rgba(0,0,0,.25)' }} />} placeholder="Type" />
                    </Form.Item>
                </Col>
            </Row>
        );

        for (var p in props) { // We generate the form from the component's properties
            var item_value = props[p];
            if (typeof elem[item_value] === 'boolean') {
                //result.push(<Form.Item key={id++}><Checkbox onChange={this.handleChange} name={item_value}>{item_value}</Checkbox></Form.Item>);
                result.push(<Form.Item key={id++}><CheckboxGroup options={plainOptions} value={this.state.checkedList} onChange={this.onChangeCheckList} /></Form.Item>);
            }else{
                if(item_value !== 'name' && item_value !== '_type' && item_value !== 'id'  && !(item_value.startsWith("_", 0))){
                    if (typeof elem[item_value] === 'object') {
                        result.push(<Form.Item key={id++} label={item_value+':'}><TextArea name={item_value}  onChange={this.handleChange} defaultValue={JSON.stringify(elem[item_value], null, 4)}  autosize={{ minRows: 1, maxRows: 15 }} /></Form.Item>);
                    }else{
                        result.push(<Form.Item key={id++} name={item_value} label={item_value+':'}><Input name={item_value}  onChange={this.handleChange} prefix={<Icon type="tag" style={{ color: 'rgba(0,0,0,.25)' }} />}  defaultValue={elem[item_value]} /></Form.Item>);
                    }
                }
            }
        }

      return (
          <Form>
                {result}
            <br/>
          </Form>
      );
    }
  }
  
  export default FormEdit;
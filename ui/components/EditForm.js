import React from "react";
import 'antd/dist/antd.css';
import {
    Checkbox, Form, Icon, Input, Tooltip, Row, Col
} from 'antd';

const { TextArea } = Input;

let id=0;

class FormEdit extends React.Component {
    
    constructor(){
      super();
      this.state = { 
        result:[],
        element: {},
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
        newElem[event.target.name]=event.target.value;
    }

    build_form(el){
        var elem=JSON.parse(JSON.stringify(el))
        this.setState({
            element: elem,
        });

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
                result.push(<Form.Item key={id++}><Checkbox onChange={this.handleChange} name={item_value}>{item_value}</Checkbox></Form.Item>);
            }else{
                if(item_value !== 'name'){
                    if(item_value !== '_type'){
                        if(item_value !== 'id'){
                            if (typeof elem[item_value] === 'object') {
                                result.push(<Form.Item key={id++} label={item_value+':'}><TextArea name={item_value}  onChange={this.handleChange} defaultValue={JSON.stringify(elem[item_value], null, 4)}  autosize={{ minRows: 1, maxRows: 15 }} /></Form.Item>);
                            }else{
                                result.push(<Form.Item key={id++} name={item_value} label={item_value+':'}><Input name={item_value}  onChange={this.handleChange} prefix={<Icon type="tag" style={{ color: 'rgba(0,0,0,.25)' }} />}  defaultValue={elem[item_value]} /></Form.Item>);
                            }
                        }
                    } 
                }
            }
        }
        this.setState({
            result: result,
        });
    }
  
  
    render() {  
      return (
          <Form>
            {this.state.result}
            <br/>
          </Form>
      );
    }
  }
  
  export default FormEdit;
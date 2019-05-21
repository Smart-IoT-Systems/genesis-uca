import React from "react";
import 'antd/dist/antd.css';
import { List, Avatar, Button } from 'antd';



class ListView extends React.Component {

    constructor(){
        super();
        this.state={
          data:[],
        };
        window.ListView=this;
    }

    componentDidMount(){
      this.refresh();
    }

    onEdit = function(){

    };

    onCall = function(){

    };

    onDelete = function(){

    };

    refresh = function(){
      const listData = [];
        var d_m=window.SiderDemo.getDM();

        d_m.components.forEach(elt => {

            var d = {};
            d.title = elt.name;
            d.description= elt._type;
            d.content= '';

            var status = "?";
            if(elt._runtime!== undefined){
              if(elt._runtime.Status !== undefined){
                status = elt._runtime.Status;
              } 
            }

            if(elt._type.indexOf("internal") > 0){
              var host = d_m.find_host(elt);
              var host_name="";
              if(host !== null){
                host_name=host.name;
              }
            }
              
            switch(elt._type) {
                case "/infra/device":
                  d.avatar = '/device.png';
                  d.content=`IP: ${elt.ip}, Port: ${elt.port[0]}, Status: ${status}, needDeployer: ${elt.isLocal}`;
                  break;
                case "/infra/vm_host":
                    d.avatar = '/server_cloud.png';
                  break;
                case "/infra/docker_host":
                    d.avatar = '/docker-official.svg';
                    d.content=`IP: ${elt.ip}, Port: ${elt.port[0]}, Status: ${status}`;
                  break;
                case "/external":
                    d.avatar = '/server_cloud.png';
                    d.content=`IP: ${elt.ip}, Port: ${elt.port[0]}, Status: ${status}`;
                  break;
                case "/internal/thingml":
                    d.avatar = '/thingml_short.png';
                    d.content=`Port: ${elt.port}, Status: ${status}, Host: Host: ${host_name}, Target: ${elt.target_language}, File: ${elt.file}`;
                  break;
                case "/internal":
                    d.avatar = '/device.png';
                    d.content=`Port: ${elt.port[0]}, Status: ${status}, Host: ${host_name}`;
                    break;
                case "/internal/node_red":
                    d.avatar = '/node-red-256.png';
                    d.content=`Port: ${elt.provided_communication_port[0].port_number}, Host: ${host_name}, Status: ${status}`;
                  break;
                default:
                    d.avatar = '/device.png';
              }

            listData.push(d);
        });
        this.setState({
          data:listData,
        });
    };

    render() {
        
        return (
            <List
                itemLayout="horizontal"
                size="large"
                dataSource={this.state.data}
                renderItem={item => (
                <List.Item
                    key={item.title}
                    actions={[
                        <Button type="primary" shape="circle" icon="edit" onClick={this.onEdit} />,
                        <Button type="default" shape="circle" icon="link" onClick={this.onCall} />,
                        <Button type="danger" shape="circle" icon="delete" onClick={this.onDelete} />,
                    ]}
                >
                    <List.Item.Meta
                    avatar={<Avatar src={'/img/' + item.avatar} />}
                    title={<a href={item.href}>{item.title}</a>}
                    description={item.description}
                    />
                    {item.content}
                </List.Item>
                )}
            />
        );
    }


}

export default ListView;
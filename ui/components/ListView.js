import React from "react";
import 'antd/dist/antd.css';
import { List, Avatar, Button } from 'antd';



class ListView extends React.Component {


    constructor(){
        super();
        window.ListView=this;
    }

    onEdit = function(){

    };

    onCall = function(){

    };

    onDelete = function(){

    };


    render() {
        
        const listData = [];
        var d_m=window.SiderDemo.getDM();

        d_m.components.forEach(elt => {

            var d = {};
            d.title = elt.name;
            d.description= elt._type;
            d.content= '';
            
            switch(elt._type) {
                case "/infra/device":
                  d.avatar = '/device.png';
                  d.content=`IP: ${elt.ip}, Port: ${elt.port[0]}, Status: , needDeployer: ${elt.isLocal}`;
                  break;
                case "/infra/vm_host":
                    d.avatar = '/server_cloud.png';
                  break;
                case "/infra/docker_host":
                    d.avatar = '/docker-official.svg';
                    d.content=`IP: ${elt.ip}, Port: ${elt.port[0]}, Status: `;
                  break;
                case "/external":
                    d.avatar = '/server_cloud.png';
                    d.content=`IP: ${elt.ip}, Port: ${elt.port[0]}, Status: `;
                  break;
                case "/internal/thingml":
                    d.avatar = '/thingml_short.png';
                    d.content=`Port: ${elt.port}, Status: , Host: , Target: ${elt.target_language}, File: ${elt.file}`;
                  break;
                case "/internal":
                    d.avatar = '/device.png';
                    d.content=`Port: ${elt.port[0]}, Status: , Host: `;
                    break;
                case "/internal/node_red":
                    d.avatar = '/node-red-256.png';
                    d.content=`Port: ${elt.provided_communication_port[0].port_number}, Host: `;
                  break;
                default:
                    d.avatar = '/device.png';
              }

            listData.push(d);
        });

        return (
            <List
                itemLayout="vertical"
                size="large"
                dataSource={listData}
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
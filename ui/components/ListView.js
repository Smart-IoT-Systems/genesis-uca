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

    onEdit = function(elt_name){
      var dm = window.SiderDemo.getDM();
      var comp = dm.find_node_named(elt_name);
      window.DrawerEdit.showDrawer(cy.$("#" + elt_name), comp);
    };

    onCall = function(){

    };

    onDelete = function(elt_name){
      var dm = window.SiderDemo.getDM();
      var comp = dm.find_node_named(elt_name);
      cy.remove("#" + elt_name); //remove from the display
      window.SiderDemo.getDM().remove_component(comp); //remove from the model together with associated links
      this.refresh();
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
                        <Button type="primary" shape="circle" icon="edit" onClick={() => this.onEdit(item.title)} />,
                        <Button type="default" shape="circle" icon="link" onClick={this.onCall} />,
                        <Button type="danger" shape="circle" icon="delete" onClick={() => this.onDelete(item.title)} />,
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
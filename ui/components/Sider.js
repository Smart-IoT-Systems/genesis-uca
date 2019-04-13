import React from "react";
import 'antd/dist/antd.css';
import {
  Layout, Menu, notification, Icon, Tabs,
} from 'antd';

import LoadModal from './LoadModal.js'
import DrawerEdit from './DrawerEdit.js'

//We load it here to avoid sync between server and client
var mm = require('../../metamodel/allinone.js');
var dm = mm.deployment_model({
    name: 'demo'
});

const TabPane = Tabs.TabPane;
const {
  Header, Content, Footer, Sider,
} = Layout;


const SubMenu = Menu.SubMenu;

class SiderDemo extends React.Component {
  
  constructor(){
    super();
    this.state = {
      collapsed: true,
      loadModalIsOpen: false,
      externalTypeRepo: [],
      internalTypeRepo: [],
    };
  }

  openNotificationWithIcon (type, title, desc){
    if(type === 'error'){
      notification[type]({
        message: title,
        description: desc,
        duration: 0,
      });
    }else{
      notification[type]({
        message: title,
        description: desc,
      });
    }
  };

  getMM(){
    return mm;
  }

  getDM(){
    return dm;
  }

  setDM(new_dm){
    dm=new_dm;
  }

  componentDidMount() {
    var repo_ext=[];
    var repo_int=[];
    fetch("/genesis/types")
      .then(response => response.json())
      .then(data => {for (var f = 0; f < data.length; f++) {
        if (data[f].isExternal) {
            repo_ext.push(data[f]);
        } else {
            repo_int.push(data[f]);
        }
      }
      this.setState({
        externalTypeRepo: repo_ext,
        internalTypeRepo: repo_int
      });
    });
  }

  showLoadModal = () => {
    this.setState({
      loadModalIsOpen: true,
    });
  }

  handleLoadModalOk = (e) => {
    var fd=e[0];
    var fr;
    fr = new FileReader();
    fr.onload = receivedText;
    fr.readAsText(fd);

    function receivedText() { //We ask the server because he is the one with all the plugins ...
      var data = JSON.parse(fr.result);
      dm = mm.deployment_model(data.dm);
      dm.components = data.dm.components;
      dm.revive_links(data.dm.links);
      cy.json(data.graph);
      editor.set(dm);
    }

    this.setState({
      loadModalIsOpen: false,
    });
  }

  handleLoadModalCancel = (e) => {
    this.setState({
      loadModalIsOpen: false,
    });
  }

  onCollapse = (collapsed) => {
    this.setState({ collapsed });
  }

  openLogs=()=>{
    window.open('/genesis/logs');
  }

  removeAll = () =>{
    var model = deployment_model({});
    fetch('/genesis/deploy', {
			method: 'POST',
			headers: {
				Accept: 'application/json',
				'Content-Type': 'application/json'
			},
			body: JSON.stringify(model)
		}).then(response => response.json())
			.then(response => { 
        if (response.success) {
          this.openNotificationWithIcon('success', 'Deployment Started', 'Empty Model sent!')
        }});
  }

  deployModel = () =>{
    fetch('/genesis/deploy', {
			method: 'POST',
			headers: {
				Accept: 'application/json',
				'Content-Type': 'application/json'
			},
			body: JSON.stringify(dm)
		}).then(response => response.json())
			.then(response => { 
        if (response.success) {
          this.openNotificationWithIcon('success', 'Deployment Started', 'Model sent!')
        }});
  }

  saveModel=()=>{
      var all_in_one = {
          dm: dm,
          graph: cy.json()
      };

      var isSafari = !!navigator.userAgent.match(/Version\/[\d\.]+.*Safari/);
      if(isSafari){
          window.open("data:text/json;charset=utf-8," + JSON.stringify(all_in_one));
      }else{
          var dataStr = "data:text/json;charset=utf-8," + encodeURIComponent(JSON.stringify(all_in_one));
          var downloadAnchorNode = document.createElement('a');
          downloadAnchorNode.setAttribute("href", dataStr);
          downloadAnchorNode.setAttribute("download", "model.json");
          document.body.appendChild(downloadAnchorNode); // required for firefox
          downloadAnchorNode.click();
          downloadAnchorNode.remove();
      }  
  }

  render() {

    var items_ext=[];
    var items_int=[];
    for(var t=0; t < this.state.internalTypeRepo.length; t++){
      var k="isc"+t;
      items_int.push(<Menu.Item key={k}>{this.state.internalTypeRepo[t].id}</Menu.Item>);
    }
    for(var t=0; t < this.state.externalTypeRepo.length; t++){
      var k2="esc"+t;
      items_ext.push(<Menu.Item key={k2}>{this.state.externalTypeRepo[t].id}</Menu.Item>);
    }

    window.SiderDemo = this;

    return (
      <Layout style={{ minHeight: '100vh' }}>
          <Header style={{ background: '#fff', padding: 0 }}>
            <Menu theme="light" mode="horizontal" style={{ lineHeight: '64px', padding: '10px 0 10 0' }}>
              <Menu.Item key="logo" disabled={true}><img style={{ height: "55px" }} src="https://enact-project.eu/img/logo-enact-blue2.png" alt="logo enact" /></Menu.Item>
              <SubMenu
                key="subFile"
                title={<span><Icon type="file" /><span>File</span></span>}
              >
                <Menu.Item key="File1" onClick={this.showLoadModal} >Load Deployment model</Menu.Item>
                <Menu.Item key="File2" onClick={this.saveModel}>Store Deployment model</Menu.Item>
              </SubMenu>
              <SubMenu
                key="subDeploy"
                title={<span><Icon type="cluster" /><span>Deploy</span></span>}
              >
                <Menu.Item key="Deploy1" onClick={this.deployModel}>Deploy</Menu.Item>
                <Menu.Item key="Deploy2" onClick={this.removeAll}>Remove All</Menu.Item>
              </SubMenu>
              <Menu.Item key="3" onClick={this.openLogs}><Icon type="ordered-list" /><span>Logs</span></Menu.Item>
            </Menu>
          </Header>
          <LoadModal visible={this.state.loadModalIsOpen} handleOk={this.handleLoadModalOk} handleCancel={this.handleLoadModalCancel}></LoadModal>
        <Layout>
        <Sider
          collapsible
          collapsed={this.state.collapsed}
          onCollapse={this.onCollapse}
        >
          <Menu theme="dark" mode="inline">
            <Menu.Item key="1"><Icon type="deployment-unit" /><span>Fleet</span></Menu.Item>
            <SubMenu
              key="sub2"
              title={<span><Icon type="edit" /><span>Edit</span></span>}
            >
              <SubMenu key="subIC" title="Infrastructure Components">
                <Menu.Item key="IC1">Device</Menu.Item>
                <Menu.Item key="IC2">Virtual Machine</Menu.Item>
                <Menu.Item key="IC3">Docker Host</Menu.Item>
              </SubMenu>
              <SubMenu key="subSC" title="Software Components">
              <SubMenu key="subSCI" title="Internal">
                <Menu.Item key="SCE1">Generic Internal Component</Menu.Item>

                {items_int}
              </SubMenu>
              <SubMenu key="subSCE" title="External">
                  <Menu.Item key="SCE1">Generic External Component</Menu.Item>
                  {items_ext}
              </SubMenu>
            </SubMenu>
              
              <SubMenu key="subLinks" title="Links">
                <Menu.Item key="Link1">Add Communication</Menu.Item>
                <Menu.Item key="Link2">Add Containment</Menu.Item>
              </SubMenu>
            </SubMenu>
            <Menu.Item key="9">
              <Icon type="file" />
              <span>TODO</span>
            </Menu.Item>
          </Menu>
        </Sider>
        <Layout>
          <Content style={{ margin: '0 16px' }}>
            <DrawerEdit />
            <div style={{ padding: 24, background: '#fff', minHeight: 360 }}>
              <Tabs tabPosition='right'>
                <TabPane tab="Graph View" key="1"><div style={{minHeight: 600, minWidth: '100px'}} id="cy"/></TabPane>
                <TabPane forceRender={true} tab="JSON View" key="2">
                  <div>
                    <button id="saveJSON">Save</button>
                    <div id="jsoneditor"></div>
                  </div>
                </TabPane>
              </Tabs>
            </div>
          </Content>
          <Footer style={{ textAlign: 'center' }}>
            ENACT ©2018
          </Footer>
        </Layout>
        </Layout>
      </Layout>
    );
  }
}

export default SiderDemo;
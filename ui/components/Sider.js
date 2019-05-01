import React from "react";
import 'antd/dist/antd.css';
import {
  Layout, Menu, notification, Icon, Tabs,
} from 'antd';

import LoadModal from './LoadModal.js'
import DrawerEdit from './DrawerEdit.js'
import AddModal from './AddModal.js'
import AddLinkModal from './AddLinkModal.js'
import AddContainmentModal from './AddContainmentModal.js'
import Notification from './Notification'
import EditContainment from './EditContainment.js'
import EditLink from './EditLink.js'

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

  showAddLinkodal = () => {
    window.AddLinkModal.showModal();
  }

  showAddContainmentModal = () => {
    window.AddContainmentModal.showModal();
  }

  showAddModal = (type, isPlugin) => {
    var modules=this.state.internalTypeRepo.concat(this.state.externalTypeRepo);
    var f = dm.node_factory();
    var elem={};
    if (!isPlugin) {
        elem = f.create_component(type, {});
    } else {
        for (var j = 0; j < modules.length; j++) {
            if (modules[j].module._type === type) {
                var tmp = JSON.stringify(modules[j].module);
                elem = JSON.parse(tmp);
                break;
            }
        }
    }
    dm.components.push(elem);
    window.AddModal.showModal(elem);
    window.FormEdit.build_form(elem);
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
      dm.revive_containments(data.dm.containments);
      cy.json(data.graph);
    }

    this.setState({
      loadModalIsOpen: false,
    });
  }

  handleChangeTab = (key) =>{
    if(key === "2"){
      editor.set(dm);
    }
  }

  handleLoadModalCancel = (e) => {
    this.setState({
      loadModalIsOpen: false,
    });
  }

  loadFromServer = () => {
    fetch("/genesis/model")
      .then(response => response.json())
      .then(data => {
        dm = mm.deployment_model(data);
        dm.components = data.components;
        dm.revive_links(data.links);
        dm.revive_containments(data.containments);
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

    window.SiderDemo = this;

    return (
      <Layout style={{ minHeight: '100vh' }}>
          <Header style={{ background: '#fff', padding: 0 }}>
            <Menu selectable={false} theme="light" mode="horizontal" style={{ lineHeight: '64px', padding: '10px 0 10 0' }}>
              <Menu.Item key="logo" disabled={true}><img style={{ height: "55px" }} src="https://enact-project.eu/img/logo-enact-blue2.png" alt="logo enact" /></Menu.Item>
              <SubMenu
                key="subFile"
                title={<span><Icon type="file" /><span>File</span></span>}
              >
                <Menu.Item key="File1" onClick={this.showLoadModal} >Load Deployment model</Menu.Item>
                <Menu.Item key="File3" onClick={this.loadFromServer}>Load deployment model from server</Menu.Item>
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
          <AddModal />
          <AddLinkModal />
          <AddContainmentModal />
        <Layout>
        <Sider
          collapsible
          collapsed={this.state.collapsed}
          onCollapse={this.onCollapse}
          width={300}
        >
          <Menu selectable={false} theme="dark" mode="inline">
            <Menu.Item key="1"><Icon type="deployment-unit" /><span>Fleet</span></Menu.Item>
            <SubMenu
              key="sub2"
              title={<span><Icon type="edit" /><span>Edit</span></span>}
            >
              <SubMenu key="subIC" title="Infrastructure Components">
                <Menu.Item onClick={() => this.showAddModal("/infra/device", false)} key="IC1">Device</Menu.Item>
                <Menu.Item onClick={() => this.showAddModal("/infra/vm_host", false)} key="IC2">Virtual Machine</Menu.Item>
                <Menu.Item onClick={() => this.showAddModal("/infra/docker_host", false)} key="IC3">Docker Host</Menu.Item>
              </SubMenu>
              <SubMenu key="subSC" title="Software Components">
                <SubMenu key="subSCI" title="Internal">
                  <Menu.Item onClick={() => this.showAddModal("/internal", false)}  key="SCE1">Generic Internal Component</Menu.Item>
                  <Menu.Item onClick={() => this.showAddModal("/internal/node_red", false)}  key="SCE2">Node-RED</Menu.Item>
                  {this.state.internalTypeRepo.map( (t) => <Menu.Item onClick={() => this.showAddModal(t.module._type, true)} key={t.id}>{t.id}</Menu.Item> )}
                </SubMenu>
                <SubMenu key="subSCE" title="External">
                    <Menu.Item onClick={() => this.showAddModal("/external", false)} key="SCE1">Generic External Component</Menu.Item>
                    {this.state.externalTypeRepo.map( (et) => <Menu.Item onClick={() => this.showAddModal(et.module._type, true)} key={et.id}>{et.id}</Menu.Item>)}
                </SubMenu>
              </SubMenu>
              
              <SubMenu key="subLinks" title="Links">
                <Menu.Item onClick={() => this.showAddLinkodal()} key="Link1">Add Communication</Menu.Item>
                <Menu.Item onClick={() => this.showAddContainmentModal()} key="Link2">Add Containment</Menu.Item>
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
            <EditContainment />
            <EditLink />
            <div style={{ padding: 24, background: '#fff', minHeight: 360 }}>
              <Tabs onChange={this.handleChangeTab} tabPosition='right'>
                <TabPane tab="Graph View" key="1"><div style={{minHeight: 600, minWidth: '100px'}} id="cy"/></TabPane>
                <TabPane forceRender={true} tab="JSON View" key="2">
                  <div>
                    <button id="saveJSON">Save</button>
                    <div id="jsoneditor"></div>
                  </div>
                </TabPane>
              </Tabs>
            </div>
            <Notification/>
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
import React from "react";
import 'antd/dist/antd.css';
import {
  Layout, Menu, notification, Icon, Tabs, message, Tooltip, Badge
} from 'antd';

import LoadModal from './LoadModal.js'
import DrawerEdit from './DrawerEdit.js'
import AddModal from './AddModal.js'
import AddLinkModal from './AddLinkModal.js'
import AddContainmentModal from './AddContainmentModal.js'
import Notification from './Notification'
import EditContainment from './EditContainment.js'
import EditLink from './EditLink.js'
import ListView from './ListView.js'
import { BrowserRouter as Router, Route, Link } from "react-router-dom";
import TestResult from "./TestResult.js";

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

const load = message;

class SiderDemo extends React.Component {

  constructor() {
    super();
    this.state = {
      collapsed: true,
      externalTypeRepo: [],
      internalTypeRepo: [],
      testShow: false
    };
    window.SiderDemo = this;
    window.loader = load;
    this.setTestShow = this.setTestShow.bind(this);
    window.testShow = this.setTestShow;
  }


  setTestShow(b) {
    this.setState({ testShow: b });
  }

  openNotificationWithIcon(type, title, desc) {
    if (type === 'error') {
      notification[type]({
        message: title,
        description: desc,
        duration: 0,
      });
    } else {
      notification[type]({
        message: title,
        description: desc,
      });
    }
  };

  getMM() {
    return mm;
  }

  getDM() {
    return dm;
  }

  setDM(new_dm) {
    dm = new_dm;
  }

  componentDidMount() {
    var repo_ext = [];
    var repo_int = [];
    fetch("/genesis/types")
      .then(response => response.json())
      .then(data => {
        for (var f = 0; f < data.length; f++) {
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
    window.LoadModal.showModal();
  }

  showAddLinkodal = () => {
    window.AddLinkModal.showModal();
  }

  showAddContainmentModal = () => {
    window.AddContainmentModal.showModal();
  }

  showAddModal = (type, isPlugin) => {
    var modules = this.state.internalTypeRepo.concat(this.state.externalTypeRepo);
    var f = dm.node_factory();
    var elem = {};
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

    window.AddModal.showModal(elem);
    //window.FormEdit.build_form(elem);
  }

  handleLoadModalOk = (e) => {
    var fd = e[0];
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

  handleChangeTab = (key) => {
    if (key === "2") {
      editor.set(dm);
    }
    if (key === "3") {
      if (window.ListView !== undefined) {
        window.ListView.refresh();
      }
    }
  }

  handleLoadModalCancel = (e) => {
    this.setState({
      loadModalIsOpen: false,
    });
  }

  loadFromServer = () => {
    fetch("/genesis/model_ui")
      .then(response => response.json())
      .then(data => {
        //First we load the model
        console.log(JSON.stringify(data))
        dm = mm.deployment_model(data.dm);
        dm.components = data.dm.components;
        dm.revive_links(data.dm.links);
        dm.revive_containments(data.dm.containments);
        //Then we update the graph
        cy.json(data.graph);
        //Including runtime status
        fetch("/genesis/runtime_info")
          .then(response => response.json())
          .then(data => {

          });
      });
  }

  onCollapse = (collapsed) => {
    this.setState({ collapsed });
  }

  openLogs = () => {
    window.open('/genesis/logs');
  }

  removeAll = () => {
    this.openNotificationWithIcon('success', 'Deployment Started', 'Empty Model sent!');
    load.loading('Deployment in progress..', 0);
    var model = mm.deployment_model({});
    fetch('/genesis/deploy', {
      method: 'POST',
      headers: {
        Accept: 'application/json',
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(model)
    }).then(response => response.json())
      .then(response => {
        if (response.started) {
          cy.elements().remove();
          dm = mm.deployment_model({
            name: 'demo'
          });
        }
      });
  }

  reset = () => {
    cy.elements().remove();
    dm = mm.deployment_model({
      name: 'demo'
    });
  }

  deploy = () => {

    load.loading('Deployment in progress..', 0);
    this.openNotificationWithIcon('success', 'Deployment Started', 'Model sent!');

    fetch("/genesis/deploy_model")
      .then(response => response.json())
      .then(response => {
        console.log(JSON.stringify(response))
      });
  };

  pushModel = () => {
    fetch('/genesis/push_model', {
      method: 'POST',
      headers: {
        Accept: 'application/json',
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(dm)
    }).then(response => response.json())
      .then(response => {
        console.log(JSON.stringify(response));

        this.openNotificationWithIcon('success', 'Model Pushed', 'Model sent!');
      });
  };

  deployModel = () => {

    load.loading('Deployment in progress..', 0);
    this.openNotificationWithIcon('success', 'Deployment Started', 'Model sent!');

    var all_in_one = {
      dm: dm,
      graph: cy.json()
    };

    fetch('/genesis/deploy', {
      method: 'POST',
      headers: {
        Accept: 'application/json',
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(all_in_one)
    }).then(response => response.json())
      .then(response => {
        if (response.started) {
          /*dm = mm.deployment_model(response.started);
          dm.components = response.started.components;
          dm.revive_links(response.started.links);
          dm.revive_containments(response.started.containments);
          this.openNotificationWithIcon('success', 'Deployment completed', 'All components are deployed!');
          load.destroy();
          load.success('Deployment finished', 5).then(()=>{
            load.destroy();
          });*/
        }
      });
  }

  saveModel = () => {
    var all_in_one = {
      dm: dm,
      graph: cy.json()
    };

    var isSafari = !!navigator.userAgent.match(/Version\/[\d\.]+.*Safari/);
    if (isSafari) {
      window.open("data:text/json;charset=utf-8," + JSON.stringify(all_in_one));
    } else {
      var dataStr = "data:text/json;charset=utf-8," + encodeURIComponent(JSON.stringify(all_in_one));
      var downloadAnchorNode = document.createElement('a');
      downloadAnchorNode.setAttribute("href", dataStr);
      downloadAnchorNode.setAttribute("download", "model.json");
      document.body.appendChild(downloadAnchorNode); // required for firefox
      downloadAnchorNode.click();
      downloadAnchorNode.remove();
    }
  }

  sendToACM = () => {
    fetch(config_ui.acm.endpoint + '/acm-model-editor/conflictCheck', {
      method: 'POST',
      headers: {
        Accept: 'application/json',
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(dm)
    }).then(response => response.json())
      .then(response => {
        if (response.newconflicts) {
          this.openNotificationWithIcon('Error', 'Actuation Conflict', 'Please check conflict using the Actuation Conflict Manager');
        } else {
          this.openNotificationWithIcon('Success', 'Actuation Conflict', 'No conflicts identified');
        }
      });
  }

  render() {

    return (
      <Router>
        <Layout style={{ minHeight: '100vh' }}>
          <Header style={{ background: '#fff', padding: 0 }}>
            <Menu selectable={false} theme="light" mode="horizontal" style={{ lineHeight: '64px', padding: '10px 0 10 0' }}>
              <Menu.Item key="logo" disabled={true}><img style={{ height: "55px" }} src="/img/Logo.png" alt="logo enact" /></Menu.Item>
              <SubMenu
                key="subFile"
                title={<span><Icon type="file" /><span>Model <Icon type="caret-down" /></span></span>}
              >
                <Menu.Item key="File1" onClick={this.showLoadModal} >Load Deployment Model from file</Menu.Item>
                <Menu.Item key="File3" onClick={this.loadFromServer}>Load Deployment Model from server</Menu.Item>
                <Menu.Divider></Menu.Divider>
                <Menu.Item key="File2" onClick={this.saveModel}>Save Deployment Model</Menu.Item>
                <Menu.Divider></Menu.Divider>
                <Menu.Item key="File4" onClick={this.reset}>Reset Editor</Menu.Item>
              </SubMenu>
              <SubMenu
                key="subDeploy"
                title={<span><Icon type="cluster" /><span>Deployment <Icon type="caret-down" /></span></span>}
              >
                <Menu.Item key="Deploy1" onClick={this.deployModel}>Deploy model in editor</Menu.Item>
                <Menu.Divider></Menu.Divider>
                <Menu.Item onClick={this.sendToACM} key="Deploy5">Check Actuation Conflicts</Menu.Item>
                <Menu.Divider></Menu.Divider>
                <Menu.Item key="Deploy2" onClick={this.pushModel}>Push model to server</Menu.Item>
                <Menu.Item key="Deploy3" onClick={this.deploy}>Deploy model on server</Menu.Item>
                <Menu.Divider></Menu.Divider>
                <Menu.Item key="Deploy4" onClick={this.removeAll}>Terminate All</Menu.Item>
              </SubMenu>
              <Menu.Item key="3" onClick={this.openLogs}><Icon type="ordered-list" /><span>Logs</span></Menu.Item>
            </Menu>
          </Header>
          <LoadModal handleOk={this.handleLoadModalOk}></LoadModal>
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
                <SubMenu
                  key="sub2"
                  title={<span><Icon type="edit" /><span>Edit</span></span>}
                >

                  <SubMenu key="subIC" title={<span><Tooltip placement="top" title="An InfrastructureComponent provides hosting facilities (i.e, it provides an execution environment) to SoftwareComponents">Infrastructure Components</Tooltip> </span>}>
                    <Menu.Item onClick={() => this.showAddModal("/infra/device", false)} key="IC1"><Tooltip placement="rightTop" title="Typically used for IoT devices">Device</Tooltip></Menu.Item>
                    <Menu.Item onClick={() => this.showAddModal("/infra/vm_host", false)} key="IC2"><Tooltip placement="rightTop" title="A virtual machine in the Cloud">Virtual Machine</Tooltip></Menu.Item>
                    <Menu.Item onClick={() => this.showAddModal("/infra/docker_host", false)} key="IC3"><Tooltip placement="rightTop" title="A Host running a Docker Engine">Docker Host</Tooltip></Menu.Item>
                  </SubMenu>

                  <SubMenu key="subSC" title={<span><Tooltip placement="top" title="A SoftwareComponent represents a piece of software to be deployed on an host. ">Software Components</Tooltip> </span>}>
                    <SubMenu key="subSCI" title={<span><Tooltip placement="top" title="A SoftwareComponent can be an InternalComponent meaning that it is managed by GeneSIS">Internal Components</Tooltip> </span>}>
                      <Menu.Item onClick={() => this.showAddModal("/internal", false)} key="SCE1"><Tooltip placement="rightTop" title="Standard Internal Component, to create your own type">Generic Internal Component</Tooltip></Menu.Item>
                      <Menu.Item onClick={() => this.showAddModal("/internal/node_red", false)} key="SCE2"><Tooltip placement="rightTop" title="An instance of a Node-RED container">Node-RED</Tooltip></Menu.Item>
                      {this.state.internalTypeRepo.map((t) => <Menu.Item onClick={() => this.showAddModal(t.module._type, true)} key={t.id}>{t.id}</Menu.Item>)}
                    </SubMenu>
                    <SubMenu key="subSCE" title={<span><Tooltip placement="top" title="A software component can be an ExternalComponent meaning that it is either managed by an external provider or hosted on a blackbox device.">External Components</Tooltip> </span>}>
                      <Menu.Item onClick={() => this.showAddModal("/external", false)} key="SCE1"><Tooltip placement="rightTop" title="Standard External Component, to create your own type">Generic External Component</Tooltip></Menu.Item>
                      {this.state.externalTypeRepo.map((et) => <Menu.Item onClick={() => this.showAddModal(et.module._type, true)} key={et.id}>{et.id}</Menu.Item>)}
                    </SubMenu>
                  </SubMenu>

                  <SubMenu key="subLinks" title="Links">
                    <Menu.Item onClick={() => this.showAddLinkodal()} key="Link1">Add Communication</Menu.Item>
                    <Menu.Item onClick={() => this.showAddContainmentModal()} key="Link2">Add Containment</Menu.Item>
                  </SubMenu>
                </SubMenu>
                <Menu.Item key="1"><Icon type="deployment-unit" /><span>Editor</span><Link to="/" /></Menu.Item>
                <Menu.Item key="9">
                  <Icon type="file" />
                  <Badge dot={this.state.testShow}><span>Test results</span></Badge>
                  <Link to="/test_results" />
                </Menu.Item>
              </Menu>
            </Sider>
            <Layout>
              <Content >
                <Route path="/test_results">
                  <TestResult data={window.lastresults} />
                </Route>
                <Route exact path="/">
                  <DrawerEdit />
                  <EditContainment />
                  <EditLink />
                  <div style={{ padding: 0, background: '#fff', minHeight: 360 }}>
                    <Tabs onChange={this.handleChangeTab} tabPosition='right'>
                      <TabPane tab={<span><Icon type="share-alt" />Graph View</span>} key="1"><div id="cy" /></TabPane>
                      <TabPane style={{ padding: 24 }} forceRender={true} tab={<span><Icon type="codepen" />JSON View</span>} key="2">
                        <div>
                          <button id="saveJSON">Save</button>
                          <div id="jsoneditor" style={{
                            width: '100%',
                            height: '500px'
                          }}></div>
                        </div>
                      </TabPane>
                      <TabPane style={{ padding: 24 }} tab={<span><Icon type="unordered-list" />List View</span>} key="3">
                        <ListView />
                      </TabPane>
                    </Tabs>
                  </div>
                  <Notification />
                </Route>
              </Content>
              <Footer style={{ textAlign: 'center' }}>
                ENACT ©2018-2021 Created by SINTEF
          </Footer>
            </Layout>
          </Layout>
        </Layout>
      </Router>
    );
  }
}

export default SiderDemo;
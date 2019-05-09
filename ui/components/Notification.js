import React from "react";
import 'antd/dist/antd.css';
import { notification } from 'antd';
import { connect } from 'mqtt';

const openNotification = (type, title, desc) => {
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
      duration: 2,
    });
  }
}

class Notification extends React.Component {

    constructor(){
        super();

        window.Notification=this;
    }

    componentDidMount(){
      const client = connect('ws://127.0.0.1:9001');
      client.on('connect', function () {
        client.subscribe('/Status');
        client.subscribe('/Notifications');
      });

      client.on('message', function (topic, message) {
        if (topic === '/Notifications') { // This is a notification
          if(message[1] === "-"){
              cy.elements().remove();
              openNotification("success", "Remove all completed!", "");
          }else{
            openNotification("success", "Notification", msg);
          }
        }else{
          var json = JSON.parse(message);
          var node = cy.getElementById(json.node);
          switch (json.status) {
            case "running":
                openNotification("success", "Node Started", "Node: " + node.id() + " sucessfully started!");
                cy.$('#' + node.id()).css({
                    'border-width': 1,
                    'border-color': '#4A4'
                });
                break;
            case "config":
                openNotification("success", "Node Configured",  node.id() + " is being configured");
                cy.$('#' + node.id()).css({
                    'border-width': 1,
                    'border-color': 'orange'
                });
                break;
            case "error":
                openNotification("error", "Error in starting", "Could not start" + node.id());
                cy.$('#' + node.id()).css({
                    'border-width': 1,
                    'border-color': '#A33'
                });
                break;
            case "OK": //It is a link
                openNotification("success", "Link configured!", "");
                cy.$('#' + node.id()).css({
                    'background-color': '#4A4',
                    'line-color': '#4A4',
                    'target-arrow-color': '#4A4',
                    'source-arrow-color': '#4A4'
                });
                break;
            case "OK": //It is a link   
                cy.$('#' + node.id()).css({
                    'background-color': 'orange',
                    'line-color': 'orange',
                    'target-arrow-color': 'orange',
                    'source-arrow-color': 'orange'
                });
                break;
            }
        }
      });
    }


    render() {
        return (
            <div></div>
        );
    }
}
export default Notification;  
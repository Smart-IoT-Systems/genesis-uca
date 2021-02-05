import React from "react";
import 'antd/dist/antd.css';
import { List, Avatar, Button } from 'antd';


class TestResult extends React.Component {

    constructor(props) {
        super(props);
        this.full = this.full.bind(this);
    }

    full() {
        return (<div style={styles.container}>
            <h1>Test Campaign: {this.props.data[0].testCampaignId}</h1>
            <List
                itemLayout="horizontal"
                size="large"
                dataSource={this.props.data}
                renderItem={item => (
                    <List.Item
                        key={item.key}
                    >
                        <List.Item.Meta
                            avatar={<Avatar src={'/img/GeneSISLogo.png'} />}
                            title={item.testCaseId}
                            description={JSON.stringify(item.scores)}
                        />
                        {item.content}
                    </List.Item>
                )}
            />
        </div>);
    }

    render() {
        let tmp = <p>No results available!</p>;
        if (window.lastresults) {
            tmp = this.full();
        }
        return tmp;
    }

}

const styles = {
    container: {
        backgroundColor: 'white',
        marginTop: 10,
        marginLeft: 10,
        marginRight: 10,
    },
};

export default TestResult;
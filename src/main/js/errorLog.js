'use strict';

import React from 'react';
import SockJsClient from 'react-stomp';

export default class ErrorLog extends React.Component {

    constructor(props) {
        super(props);
        this.state = {messages: []};
        this.logDiv = React.createRef();
        this.onMessage = this.onMessage.bind(this);
    }

    componentDidUpdate() {
        this.logDiv.current.scrollTop = this.logDiv.current.scrollHeight;
    }

    onMessage(message) {
        const messages = this.state.messages;
        messages.push(<div>{new Date(message.timestamp).toLocaleString()}: {message.error}</div>);
        this.setState({messages: messages});
    }

    render() {
        return (
            <div>
                <SockJsClient
                    url={'http://localhost:8080/events'}
                    topics={['/topic/error']}
                    onMessage={msg => this.onMessage(msg)}/>
                <a href="#errorLog" className="button inline">Errors</a>
                <div id="errorLog" className="overlay">
                    <div className="popup big">
                        <h2>Errors</h2>
                        <a href="#" className="close">X</a>
                        <div ref={this.logDiv} className="log">
                            {this.state.messages}
                        </div>
                    </div>
                </div>
            </div>
        );
    }
}

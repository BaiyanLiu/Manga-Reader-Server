'use strict';

import React from 'react';
import SockJsClient from 'react-stomp';

export default class ErrorLog extends React.Component {

    constructor(props) {
        super(props);
        this.hasLoaded = false;
        this.state = {messages: [], pageSize: 100};
        this.logDiv = React.createRef();
        this.handleShow = this.handleShow.bind(this);
        this.onMessage = this.onMessage.bind(this);
    }

    componentDidUpdate() {
        this.logDiv.current.scrollTop = this.logDiv.current.scrollHeight;
    }

    handleShow(e) {
        e.preventDefault();
        if (!this.hasLoaded) {
            fetch(`api/errorMessages?sort=id,desc&size=${this.state.pageSize}`)
                .then(response => response.json())
                .then(data => {
                    this.hasLoaded = true;
                    const messages = Object.keys(data._embedded.errorMessages).map(i => {
                        return data._embedded.errorMessages[i]
                    });
                    messages.reverse();
                    this.setState({messages: messages});
                });
        }
        window.location = e.currentTarget.href;
    }

    onMessage(message) {
        if (!this.hasLoaded) {
            return;
        }
        const messages = this.state.messages;
        messages.push(message);
        this.setState({messages: messages});
    }

    render() {
        const messages = this.state.messages.map(message => {
            return <div className="line">{new Date(message.timestamp).toLocaleString()}: {message.error}</div>;
        });
        return (
            <div>
                <SockJsClient
                    url={'http://localhost:8080/events'}
                    topics={['/topic/error']}
                    onMessage={msg => this.onMessage(msg)}/>
                <a href={"#errorLog"} onClick={this.handleShow} className="button-inline">Errors</a>
                <div id="errorLog" className="overlay">
                    <div className="popup-big">
                        <h2>Errors</h2>
                        <a href="#" className="close">X</a>
                        <div ref={this.logDiv} className="log">
                            {messages}
                        </div>
                    </div>
                </div>
            </div>
        );
    }
}

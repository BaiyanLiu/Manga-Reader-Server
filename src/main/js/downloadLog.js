'use strict';

import React from 'react';
import SockJsClient from 'react-stomp';

export default class DownloadLog extends React.Component {

    constructor(props) {
        super(props);
        this.hasLoaded = false;
        this.state = {messages: {}, messageIds: [], pageSize: 100};
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
            fetch(`api/downloadMessages?sort=id,desc&size=${this.state.pageSize}`)
                .then(response => response.json())
                .then(data => {
                    this.hasLoaded = true;
                    const messages = {};
                    const messageIds = [];
                    this.addMessages(messages, messageIds, data._embedded.downloadMetadataMessages);
                    this.addMessages(messages, messageIds, data._embedded.downloadChapterMessages);
                    messageIds.sort((a, b) => parseInt(a) - parseInt(b));
                    this.setState({messages: messages, messageIds: messageIds});
                });
        }
        window.location = e.currentTarget.href;
    }

    addMessages(messages, messageIds, data) {
        if (data) {
            Object.keys(data).map(i => {
                messages[data[i].id] = data[i];
                messageIds.push(data[i].id);
            });
        }
    }

    onMessage(message) {
        if (!this.hasLoaded) {
            return;
        }
        const messages = this.state.messages;
        const messageIds = this.state.messageIds;
        if (!(message.id in messages)) {
            messageIds.push(message.id);
        }
        messages[message.id] = message;
        this.setState({messages: messages, messageIds: messageIds});
    }

    getStyle(message) {
        if (message.status === "STARTED") {
            return "";
        } else {
            return " " + message.status.toLowerCase();
        }
    }

    formatMessage(message) {
        const header = `${new Date(message.timestamp).toLocaleString()}: ${message.mangaName}`;
        const footer = ` | ${message.completed} of ${message.total}`;
        if (message.chapter) {
            return header + ` | Chapter ${message.chapter}` + footer;
        } else {
            return header + footer;
        }
    }

    addControls(message) {
        if (message._links) {
            if (message._links.cancel) {
                return <a className="button-right" onClick={() => fetch(message._links.cancel.href).then(() => {})}>Cancel</a>;
            } else if (message._links.resolve) {
                return <a className="button-right" onClick={() => fetch(message._links.resolve.href).then(() => {})}>Resolve</a>;
            }
        }
    }

    render() {
        const messages = this.state.messageIds.map(id => {
            const message = this.state.messages[id];
            return <div className={"line" + this.getStyle(message)}>{this.formatMessage(message)}{this.addControls(message)}</div>;
        });
        return (
            <div>
                <SockJsClient
                    url={'http://localhost:8080/events'}
                    topics={['/topic/download']}
                    onMessage={message => this.onMessage(message)}/>
                <a href={"#downloadLog"} onClick={this.handleShow} className="button-left">Downloads</a>
                <div id="downloadLog" className="overlay">
                    <div className="popup-big">
                        <h2>Downloads</h2>
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

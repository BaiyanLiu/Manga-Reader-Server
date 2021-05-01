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
                    const messages = {}
                    this.addMessages(messages, data._embedded.downloadMetadataMessages);
                    this.addMessages(messages, data._embedded.downloadChapterMessages);
                    this.addMessages(messages, data._embedded.downloadPageMessages);
                    const messageIds = []
                    Object.keys(messages).map(i => {
                        messageIds.push(messages[i].id);
                    });
                    messageIds.sort((a, b) => parseInt(a) - parseInt(b));
                    this.setState({messages: messages, messageIds: messageIds});
                });
        }
        window.location = e.currentTarget.href;
    }

    addMessages(messages, data) {
        if (data) {
            Object.keys(data).map(i => {
                messages[data[i].id] = data[i]
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

    formatMessage(message) {
        if (message.page) {
            return this.formatDownloadPageMessage(message);
        } else if (message.chapter) {
            return this.formatDownloadChapterMessage(message);
        } else {
            return this.formatDownloadMetadataMessage(message);
        }
    }

    formatDownloadMetadataMessage(message) {
        return `${new Date(message.timestamp).toLocaleString()}: ${message.status === "START" ? "Start" : "End"} | ${message.mangaName}`
    }

    formatDownloadChapterMessage(message) {
        return `${this.formatDownloadMetadataMessage(message)} | Chapter ${message.chapter}`
    }

    formatDownloadPageMessage(message) {
        return `${this.formatDownloadChapterMessage(message)} | Page ${message.page}`
    }

    render() {
        const messages = this.state.messageIds.map(id => <div>{this.formatMessage(this.state.messages[id])}</div>);
        return (
            <div>
                <SockJsClient
                    url={'http://localhost:8080/events'}
                    topics={['/topic/download']}
                    onMessage={msg => this.onMessage(msg)}/>
                <a href="#downloadLog" onClick={this.handleShow} className="button inline inline-margin float-left">Downloads</a>
                <div id="downloadLog" className="overlay">
                    <div className="popup big">
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

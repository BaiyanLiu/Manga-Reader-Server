'use strict';

import React from 'react';
import SockJsClient from 'react-stomp';

export default class DownloadLog extends React.Component {

    constructor(props) {
        super(props);
        this.state = {messages: []};
        this.logDiv = React.createRef();
        this.onDownloadMetadata = this.onDownloadMetadata.bind(this);
        this.onDownloadChapter = this.onDownloadChapter.bind(this);
        this.onDownloadPage = this.onDownloadPage.bind(this);
    }

    componentDidUpdate() {
        this.logDiv.current.scrollTop = this.logDiv.current.scrollHeight;
    }

    onDownloadMetadata(message) {
        const messages = this.state.messages;
        messages.push(<div>{this.formatDownloadMetadataMessage(message)}</div>);
        this.setState({messages: messages});
    }

    onDownloadChapter(message) {
        const messages = this.state.messages;
        messages.push(<div>{this.formatDownloadChapterMessage(message)}</div>);
        this.setState({messages: messages});
    }

    onDownloadPage(message) {
        const messages = this.state.messages;
        messages.push(<div>{this.formatDownloadPageMessage(message)}</div>);
        this.setState({messages: messages});
    }

    formatDownloadMetadataMessage(message) {
        return `${new Date(message.timestamp).toLocaleString()}: ${message.status === "STARTED" ? "Start" : "End"} | ${message.manga.name}`
    }

    formatDownloadChapterMessage(message) {
        return `${this.formatDownloadMetadataMessage(message)} | Chapter ${message.chapter}`
    }

    formatDownloadPageMessage(message) {
        return `${this.formatDownloadChapterMessage(message)} | Page ${message.page}`
    }

    render() {
        return (
            <div>
                <SockJsClient
                    url={'http://localhost:8080/events'}
                    topics={['/topic/download/metadata']}
                    onMessage={msg => this.onDownloadMetadata(msg)}/>
                <SockJsClient
                    url={'http://localhost:8080/events'}
                    topics={['/topic/download/chapter']}
                    onMessage={msg => this.onDownloadChapter(msg)}/>
                <SockJsClient
                    url={'http://localhost:8080/events'}
                    topics={['/topic/download/page']}
                    onMessage={msg => this.onDownloadPage(msg)}/>
                <a href="#downloadLog" className="button">Downloads</a>
                <div id="downloadLog" className="overlay">
                    <div className="popup big">
                        <h2>Downloads</h2>
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

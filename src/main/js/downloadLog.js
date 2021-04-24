'use strict';

import React from 'react';
import SockJsClient from 'react-stomp';

export default class DownloadLog extends React.Component {

    constructor(props) {
        super(props);
        this.state = {messages: []};
        this.logDiv = React.createRef();
        this.onDownloadMetadataMessageReceived = this.onDownloadMetadataMessageReceived.bind(this);
    }

    componentDidUpdate() {
        this.logDiv.current.scrollTop = this.logDiv.current.scrollHeight;
    }

    onDownloadMetadataMessageReceived(message) {
        const messages = this.state.messages;
        messages.push(`${new Date(message.timestamp).toLocaleString()}: ${message.status === "STARTED" ? "Downloading" : "Finished downloading"} metadata for ${message.manga.name}\n`)
        this.setState({messages: messages});
    }

    render() {
        return (
            <div>
                <SockJsClient
                    url={'http://localhost:8080/events'}
                    topics={['/topic/download/metadata']}
                    onMessage={msg => this.onDownloadMetadataMessageReceived(msg)}/>
                <a href="#downloadLog" className="button">Download log</a>
                <div id="downloadLog" className="overlay">
                    <div className="popup big">
                        <h2>Download log</h2>
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

'use strict';

import React from 'react';
import Page from './page';
import SockJsClient from "react-stomp";

export default class ChapterList extends React.Component {

    constructor(props) {
        super(props);
        this.hasLoaded = false;
        this.state = {chapters: {}, chapterNumbers: []};
        this.handleToggle = this.handleToggle.bind(this);
        this.onMessage = this.onMessage.bind(this);
    }

    handleToggle(e) {
        e.preventDefault();
        if (!this.hasLoaded) {
            fetch(this.props.manga._links.chapters.href)
                .then(response => response.json())
                .then(data => {
                    this.hasLoaded = true;
                    const chapters = {};
                    const chapterNumbers = [];
                    data._embedded.chapters.map(chapter => {
                        chapters[chapter.number] = chapter;
                        chapterNumbers.push(chapter.number);
                    });
                    chapterNumbers.sort((a, b) => parseFloat(b) - parseFloat(a));
                    this.setState({chapters: chapters, chapterNumbers: chapterNumbers});
                });
        }
        const collapsible = e.currentTarget.nextElementSibling.style;
        if (collapsible.display === "block") {
            collapsible.display = "none";
        } else {
            collapsible.display = "block";
        }
    }

    onMessage(message) {
        if (!this.hasLoaded || message.mangaId !== this.props.manga.id) {
            return;
        }
        const chapters = this.state.chapters;
        const chapterNumbers = this.state.chapterNumbers;
        message.chapters.map(chapter => {
            if (!(chapter.number in chapters)) {
                chapterNumbers.push(chapter.number);
            }
            chapters[chapter.number] = chapter;
        });
        chapterNumbers.sort((a, b) => parseFloat(b) - parseFloat(a));
        this.setState({chapters: chapters, chapterNumbers: chapterNumbers});
    }

    render() {
        const chapters = this.state.chapterNumbers.map(number =>
            <Chapter
                key={`chapter-${this.props.manga.id}-${number}`}
                manga={this.props.manga}
                chapter={this.state.chapters[number]}/>
        );
        return (
            <div className="inline">
                <SockJsClient
                    url={'http://localhost:8080/events'}
                    topics={['/topic/chapter']}
                    onMessage={message => this.onMessage(message)}/>
                <a onClick={this.handleToggle} className="button">...</a>
                <div className="collapsible">
                    <hr/>
                    {chapters}
                </div>
            </div>
        );
    }
}

class Chapter extends React.Component {

    constructor(props) {
        super(props);
        this.handleDownload = this.handleDownload.bind(this);
    }

    handleDownload(e) {
        e.preventDefault();
        fetch(this.props.chapter._links.download.href).then(() => {});
    }

    render() {
        return (
            <div className="chapter">
                <div className={"inline-margin" + (this.props.chapter.read ? " read" : this.props.chapter.downloaded ? "" : " new")}>
                    {this.props.chapter.name}
                </div>
                <div className="controls">
                    <a onClick={this.handleDownload} className="button-inline">DL</a>
                    <Page
                        key={`page-${this.props.manga.id}-${this.props.chapter.number}`}
                        manga={this.props.manga}
                        chapter={this.props.chapter}
                        page={1}/>
                </div>
            </div>
        );
    }
}

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
        this.onUpdate = this.onUpdate.bind(this);
    }

    handleToggle(e) {
        e.preventDefault();
        if (!this.hasLoaded) {
            fetch(`api/chapters?manga=${this.props.manga.id}`)
                .then(response => response.json())
                .then(data => {
                    this.hasLoaded = true;
                    const chapters = {};
                    const chapterNumbers = [];
                    Object.keys(data).map(i => {
                        chapters[data[i].number] = data[i];
                        chapterNumbers.push(data[i].number);
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

    onUpdate(chapter) {
        if (!this.hasLoaded) {
            return;
        }
        const chapters = this.state.chapters;
        const chapterNumbers = this.state.chapterNumbers;
        if (!(chapter.number in chapters)) {
            chapterNumbers.push(chapter.number);
            chapterNumbers.sort((a, b) => parseFloat(b) - parseFloat(a));
        }
        chapters[chapter.number] = chapter;
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
                    onMessage={chapter => this.onUpdate(chapter)}/>
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
        fetch(`api/downloadChapter?manga=${this.props.manga.id}&chapter=${this.props.chapter.number}`).then(() => {});
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

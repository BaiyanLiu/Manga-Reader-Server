'use strict';

import React from 'react';
import Page from './page';

export default class ChapterList extends React.Component {

    constructor(props) {
        super(props);
        this.hasLoaded = false;
        this.state = {chapters: []};
        this.handleToggle = this.handleToggle.bind(this);
    }

    handleToggle(e) {
        e.preventDefault();
        if (!this.hasLoaded) {
            fetch(`api/chapters?manga=${this.props.manga.id}`)
                .then(response => response.json())
                .then(data => {
                    this.hasLoaded = true;
                    this.setState({chapters: data});
                });
        }
        const collapsible = e.currentTarget.nextElementSibling.style;
        if (collapsible.display === "block") {
            collapsible.display = "none";
        } else {
            collapsible.display = "block";
        }
    }

    render() {
        const chapters = Object.keys(this.state.chapters).map(chapter =>
            <Chapter
                key={`chapter-${this.props.manga.id}-${this.state.chapters[chapter].number}`}
                manga={this.props.manga}
                chapter={this.state.chapters[chapter]}/>
        );
        return (
            <div className="inline">
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
                <div className="inline inline-margin">
                    {this.props.chapter.name}
                </div>
                <a onClick={this.handleDownload} className="button inline inline-margin">D</a>
                <Page
                    key={`page-${this.props.manga.id}-${this.props.chapter.number}`}
                    manga={this.props.manga}
                    chapter={this.props.chapter}
                    page={1}/>
            </div>
        );
    }
}
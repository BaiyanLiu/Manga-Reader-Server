'use strict';

import React from 'react';
import ChapterList from "./chapterList";

export default class MangaList extends React.Component {

    constructor(props) {
        super(props);
        this.handleFirst = this.handleFirst.bind(this);
        this.handlePrev = this.handlePrev.bind(this);
        this.handleNext = this.handleNext.bind(this);
        this.handleLast = this.handleLast.bind(this);
    }

    handleFirst(e){
        e.preventDefault();
        this.props.onNavigate(this.props.links.first.href);
    }

    handlePrev(e) {
        e.preventDefault();
        this.props.onNavigate(this.props.links.prev.href);
    }

    handleNext(e) {
        e.preventDefault();
        this.props.onNavigate(this.props.links.next.href);
    }

    handleLast(e) {
        e.preventDefault();
        this.props.onNavigate(this.props.links.last.href);
    }

    render() {
        const mangas = this.props.mangas.map(manga =>
            <Manga
                key={manga._links.self.href}
                manga={manga}
                fields={this.props.fields}
                onUpdate={this.props.onUpdate}
                onDelete={this.props.onDelete}/>
        );

        const navigation = [];
        if ("first" in this.props.links) {
            navigation.push(<a key="first" onClick={this.handleFirst} className="button inline-margin">&lt;&lt;</a>);
        }
        if ("prev" in this.props.links) {
            navigation.push(<a key="prev" onClick={this.handlePrev} className="button inline-margin">&lt;</a>);
        }
        if ("next" in this.props.links) {
            navigation.push(<a key="next" onClick={this.handleNext} className="button inline-margin">&gt;</a>);
        }
        if ("last" in this.props.links) {
            navigation.push(<a key="last" onClick={this.handleLast} className="button inline-margin">&gt;&gt;</a>);
        }

        return (
            <div>
                <table>
                    <tbody>
                    <tr>
                        <th>Name</th>
                        <th>Source</th>
                        <th/>
                        <th/>
                    </tr>
                    {mangas}
                    </tbody>
                </table>
                <br/>
                <div>
                    {navigation}
                </div>
            </div>
        );
    }
}

class Manga extends React.Component {

    constructor(props) {
        super(props);
        this.handleDelete = this.handleDelete.bind(this);
    }

    handleDelete(e) {
        e.preventDefault();
        this.props.onDelete(this.props.manga);
    }

    render() {
        return (
            <tr>
                <td>
                    <div className="inline inline-margin">
                        {this.props.manga.name}
                    </div>
                    <ChapterList manga={this.props.manga}/>
                </td>
                <td>{this.props.manga.source}</td>
                <td>
                    <UpdateDialog
                        manga={this.props.manga}
                        fields={this.props.fields}
                        onUpdate={this.props.onUpdate}/>
                </td>
                <td>
                    <a onClick={this.handleDelete} className="button negative">Delete</a>
                </td>
            </tr>
        );
    }
}

class UpdateDialog extends React.Component {

    constructor(props) {
        super(props);
        this.handleSubmit = this.handleSubmit.bind(this);
    }

    handleSubmit(e) {
        e.preventDefault();
        const manga = {};
        this.props.fields.forEach(field => {
            manga[field] = ReactDOM.findDOMNode(this.refs[field]).value.trim();
        });
        this.props.onUpdate(this.props.manga, manga);
        window.location = "#";
    }

    render() {
        const inputs = this.props.fields.map(field =>
            <p key={field}>
                <input type="text" placeholder={field} defaultValue={this.props.manga[field]} ref={field} className="field"/>
            </p>
        );
        const id = "updateManga-" + this.props.manga._links.self.href;
        return (
            <div key={this.props.manga._links.self.href}>
                <a href={"#" + id} className="button">Update</a>
                <div id={id} className="overlay">
                    <div className="popup">
                        <h2>Update manga</h2>
                        <a href="#" title="Close" className="close">X</a>
                        <form>
                            {inputs}
                            <a onClick={this.handleSubmit} className="button positive">Update</a>
                        </form>
                    </div>
                </div>
            </div>
        );
    }
}

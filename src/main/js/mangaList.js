'use strict';

import React from 'react';
import ReactDOM from 'react-dom';
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
        this.props.mangas.sort((a, b) => {
            return (a.unread === 0) - (b.unread === 0)
                || (a.lastRead === null) - (b.lastRead === null)
                || b.lastRead > a.lastRead
        });
        const mangas = this.props.mangas.map(manga =>
            <Manga
                key={manga._links.self.href}
                manga={manga}
                fields={this.props.fields}
                sources={this.props.sources}
                showAll={this.props.showAll}
                onUpdate={this.props.onUpdate}
                onDelete={this.props.onDelete}/>
        );

        const navigation = [];
        if ("first" in this.props.links) {
            navigation.push(<a key="first" onClick={this.handleFirst} className="button-inline">&lt;&lt;</a>);
        }
        if ("prev" in this.props.links) {
            navigation.push(<a key="prev" onClick={this.handlePrev} className="button-inline">&lt;</a>);
        }
        if ("next" in this.props.links) {
            navigation.push(<a key="next" onClick={this.handleNext} className="button-inline">&gt;</a>);
        }
        if ("last" in this.props.links) {
            navigation.push(<a key="last" onClick={this.handleLast} className="button-inline">&gt;&gt;</a>);
        }

        return (
            <div>
                <table>
                    <tbody>
                    <tr>
                        <th>Name</th>
                        <th>Source</th>
                        <th>Last Read</th>
                        <th>Unread</th>
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
        const textStyle = this.props.manga.read ? " read" : "";
        return (
            <tr>
                <td>
                    <div className={"inline-margin" + textStyle}>
                        {this.props.manga.name}
                    </div>
                    <a onClick={() => fetch(this.props.manga._links.update.href)} className="button-inline">UPD</a>
                    <a onClick={() => fetch(this.props.manga._links.download.href)} className="button-inline">DL</a>
                    <ChapterList
                        manga={this.props.manga}
                        showAll={this.props.showAll}/>
                </td>
                <td className={textStyle}>{this.props.manga.source}</td>
                <td className={textStyle}>{new Date(this.props.manga.lastRead).toLocaleString()}</td>
                <td className={textStyle}>{this.props.manga.unread}</td>
                <td>
                    <UpdateDialog
                        manga={this.props.manga}
                        fields={this.props.fields}
                        sources={this.props.sources}
                        onUpdate={this.props.onUpdate}/>
                </td>
                <td>
                    <a onClick={this.handleDelete} className="button-negative">Delete</a>
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
        const sources = this.props.sources.map(source => <option value={source}>{source}</option>);
        const inputs = this.props.fields.map(field => {
            if (field === "source") {
                return <p><select ref={field}>{sources}</select></p>;
            } else {
                return <p>
                            <input type="text"
                                   placeholder={field.charAt(0).toUpperCase() + field.slice(1)}
                                   defaultValue={this.props.manga[field]}
                                   readOnly={field === "name"}
                                   ref={field}/>
                        </p>;
            }
        });

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
                            <a onClick={this.handleSubmit} className="button-positive">Update</a>
                        </form>
                    </div>
                </div>
            </div>
        );
    }
}

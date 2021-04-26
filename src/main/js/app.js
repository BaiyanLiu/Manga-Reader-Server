'use strict';

import React from 'react';
import ReactDOM from 'react-dom';
import MangaList from "./mangaList";
import DownloadLog from "./downloadLog";

class App extends React.Component {

    constructor(props) {
        super(props);
        this.root = 'api/mangas';
        this.state = {mangas: [], links: [], fields: ['name', 'source', 'sourceId'], pageSize: 20};
        this.home = `${this.root}?size=${encodeURIComponent(this.state.pageSize)}`;
        this.onNavigate = this.onNavigate.bind(this);
        this.onCreate = this.onCreate.bind(this);
        this.onUpdate = this.onUpdate.bind(this);
        this.onDelete = this.onDelete.bind(this);
    }

    onNavigate(uri) {
        fetch(uri)
            .then(response => response.json())
            .then(data => {
                this.links = data._links;
                return data._embedded.mangas;
            })
            .then(mangas => Promise.all(mangas.map(manga => fetch(manga._links.self.href))))
            .then(response => Promise.all(response.map(resp =>
                resp.json()
                    .then(data => {
                        data.headers = resp.headers
                        return data
                    })
            )))
            .then(data => {
                this.setState({
                    mangas: data,
                    links: this.links
                })
            });
    }

    onCreate(manga) {
        const requestOptions = {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify(manga)
        };
        fetch(this.root, requestOptions)
            .then(() => this.onNavigate(this.home));
    }

    onUpdate(manga, update) {
        const requestOptions = {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
                'If-Match': manga.headers.get('Etag')
            },
            body: JSON.stringify(update)
        };
        fetch(manga._links.self.href, requestOptions)
            .then(response => {
                if (response.status === 412) {
                    alert(`412 - ${manga._links.self.href}`)
                }
                this.onNavigate(this.home)
            });
    }

    onDelete(manga) {
        fetch(manga._links.self.href, {method: 'DELETE'})
            .then(() => this.onNavigate(this.home));
    }

    handleKeyDown(e) {
        if (e.key === "Escape") {
            e.preventDefault()
            window.location = "#";
        }
    }

    componentDidMount() {
        document.addEventListener("keydown", this.handleKeyDown)
        this.onNavigate(this.home);
    }

    render() {
        return (
            <div>
                <DownloadLog/>
                <br/>
                <CreateDialog
                    fields={this.state.fields}
                    onCreate={this.onCreate}/>
                <br/>
                <MangaList
                    mangas={this.state.mangas}
                    fields={this.state.fields}
                    links={this.state.links}
                    onNavigate={this.onNavigate}
                    onUpdate={this.onUpdate}
                    onDelete={this.onDelete}/>
            </div>
        );
    }
}

class CreateDialog extends React.Component {

    constructor(props) {
        super(props);
        this.handleSubmit = this.handleSubmit.bind(this);
    }

    handleSubmit(e) {
        e.preventDefault();
        const manga = {};
        this.props.fields.forEach(field => {
            let input = ReactDOM.findDOMNode(this.refs[field]);
            manga[field] = input.value.trim();
            input.value = '';
        });
        this.props.onCreate(manga);
        window.location = "#";
    }

    render() {
        const inputs = this.props.fields.map(field =>
            <p key={field}>
                <input type="text" placeholder={field.charAt(0).toUpperCase() + field.slice(1)} ref={field} className="input"/>
            </p>
        );
        return (
            <div>
                <a href="#createManga" className="button">Create</a>
                <div id="createManga" className="overlay">
                    <div className="popup">
                        <h2>Create manga</h2>
                        <a href="#" className="close">X</a>
                        <form>
                            {inputs}
                            <a onClick={this.handleSubmit} className="button positive">Create</a>
                        </form>
                    </div>
                </div>
            </div>
        );
    }
}

ReactDOM.render(
    <App />,
    document.getElementById('react')
);

'use strict';

import React from 'react';

export default class Page extends React.Component {

    constructor(props) {
        super(props);
        this.hasLoaded = false;
        this.state = {page: {image: {}}};
        this.pageDiv = React.createRef();
        this.handleKeyDown = this.handleKeyDown.bind(this);
        this.handleShow = this.handleShow.bind(this);
        this.handleHide = this.handleHide.bind(this);
    }

    componentDidUpdate() {
        this.pageDiv.current.scrollTo(0, 0);
    }

    handleKeyDown(e) {
        let page = this.state.page.number;
        if (e.key === "ArrowLeft" && page > 1) {
            page--;
        } else if (e.key === "ArrowRight") {
            page++;
        }
        if (page !== this.state.page.number) {
            e.preventDefault()
            this.navigate(page)
        }
    }

    handleShow(e) {
        e.preventDefault();
        document.addEventListener("keydown", this.handleKeyDown)
        if (!this.hasLoaded) {
            this.navigate(this.props.page);
        }
        window.location = e.currentTarget.href;
    }

    handleHide(e) {
        e.preventDefault();
        document.removeEventListener("keydown", this.handleKeyDown)
        window.location = e.currentTarget.href;
    }

    navigate(page) {
        fetch(`api/page?manga=${this.props.manga.id}&chapter=${this.props.chapter.number}&page=${page}`)
            .then(response => response.json())
            .then(data => {
                this.hasLoaded = true;
                this.setState({page: data});
            });
    }

    render() {
        const id = `page-${this.props.manga.id}-${this.props.chapter.number}`;
        return (
            <div key={id} className="inline">
                <a href={"#" + id} onClick={this.handleShow} className="button">...</a>
                <div id={id} className="overlay">
                    <div className="popup page">
                        <h2>Page {this.state.page.number}</h2>
                        <a href="#" onClick={this.handleHide} title="Close" className="close">X</a>
                        <div ref={this.pageDiv} className="image">
                            <img src={"data:image/jpg;base64," + this.state.page.image.data} alt="Loading..."/>
                        </div>
                    </div>
                </div>
            </div>
        );
    }
}

import React, { Component } from 'react';
import {Link, BrowserRouter} from 'react-router-dom';

class Header extends Component {
    render() {
        return (
            <header role="banner">
                <section className="header no-topbar">
                    <Link id="logo" to="/" title="Velocorner" />
                    <nav>
                        <ul>
                            <li><Link to="/" className={ this.props.page == "main" ? 'current' : '' }>Home</Link></li>
                            <li><Link to="/about" className={ this.props.page == "about" ? 'current' : '' }>About</Link></li>
                        </ul>
                    </nav>
                </section>
            </header>
        )
    }
}

export default Header;
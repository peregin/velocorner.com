import React, { Component } from 'react';
import {Link} from 'react-router-dom';

class Header extends Component {
    render() {
        return (
            <div className="header no-topbar">
                <br/>
                
                <div className="navbar navbar-default mega-menu navbar-responsive-collapse" role="navigation">
                    <div className="container">

                        {/* Brand and toggle get grouped for better mobile display */}
                        <div className="navbar-header">
                            <button type="button" className="navbar-toggle" data-toggle="collapse" data-target=".navbar-responsive-collapse">
                                <span className="sr-only">Toggle navigation</span>
                                <span className="fa fa-bars"></span>
                            </button>
                            <h2>
                                <a className="navbar-brand VC_nopadding" href="/">
                                    <img id="logo-header" className="VC_logo_size" src="/images/logo50.png" alt="Logo1"/>
                                </a>
                                <a className="navbar-brand" href="/">VELOCORNER</a>
                            </h2>
                        </div>

                        {/*  Search input form */}
                        {/* TODO: shortcut - only if logged in */}
                        <div className="input-group col-md-6 col-xs-12" style={{float: 'left', margin: '10px'}}>
                            <input id="search" name="search" type="text" className="form-control" placeholder="Search for activities ..."/>
                            <span className="input-group-btn">
                                <button id="search_button" name="search_button" className="btn-u" type="button"><i className="fa fa-search"></i></button>
                            </span>
                        </div>
                    

                        {/* Collect the nav links, forms, and other content for toggling */}
                        <div className="collapse navbar-collapse navbar-responsive-collapse">
                            <ul className="nav navbar-nav">
                                {/* Home */}
                                <li className={ this.props.page == "Home" ? 'active' : '' }><a href="/">Home</a></li>
                                {/* About */}
                                <li className={ this.props.page == "About" ? 'active' : '' }><a href="/about">About Us</a></li>
                            </ul>
                        </div> {/* navbar-collapse */}
                    </div>
                </div>

                {/* <script src="%PUBLIC_URL%/javascripts/search.js" type="text/javascript"></script> */}

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
            </div>
        )
    }
}

export default Header;
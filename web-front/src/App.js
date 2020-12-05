import React, { Component } from 'react';
import {
  BrowserRouter as Router, Route,
} from 'react-router-dom';

import Home from "./pages/Home";
import About from "./pages/About";

import Header from './components/Header';
import Footer from './components/Footer';

import ApiClient from "./service/ApiClient";

import './App.css';

class App extends Component {
  constructor(props) {
    super(props);
    this.state = { title: 'Velocorner' };
  }

  async componentDidMount() {
    ApiClient.getStatus(summary => {
      this.setState({
        title: 'Velocorner, Memory Usage: ' + summary.memoryUsedPercentile + '%'
      });
    });
  }

  render() {
    return (
      <Router>
        <div className="wrapper">

          <Header />

          <h1>Welcome to {this.state.title}!</h1>

          <Route exact path="/" component={Home} />
          <Route path="/about" component={About} />

          <Footer />

        </div>
      </Router>
    )
  }
}

export default App;

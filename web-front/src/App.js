import React, { Component } from 'react';
import {
  BrowserRouter, Route, Routes,
} from 'react-router-dom';

import { ChakraProvider, Progress } from '@chakra-ui/react'

import Home from "./pages/Home";
import Best from "./pages/Best";
import About from "./pages/About";

import Header from './components/Header';
import Footer from './components/Footer';

import ApiClient from "./service/ApiClient";

import './App.css';

class App extends Component {
  constructor(props) {
    super(props);
    this.state = { memoryUsage: 50 };
  }

  async componentDidMount() {
    ApiClient.getStatus(summary => {
      console.log(summary)
      this.setState({
        memoryUsage: summary.memoryUsedPercentile
      });
    });
  }

  render() {
    return (
      <ChakraProvider>
        <BrowserRouter>
          <div className="wrapper">

            <Header />

            <h1>Welcome to Velocorner, memory usage {this.state.memoryUsage}%</h1>
            <Progress hasStripe value={this.state.memoryUsage}/>

            <Routes>
              <Route exact path="/" element={<Home />} />
              <Route path="/about" element={<About />} />
              <Route path="/best" element={<Best />} />
            </Routes>

            <Footer />

          </div>
        </BrowserRouter>
      </ChakraProvider>
    )
  }
}

export default App;

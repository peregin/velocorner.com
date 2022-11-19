import React, { Component } from 'react';
import {
  BrowserRouter, Route, Routes,
} from 'react-router-dom';

import { ChakraProvider } from '@chakra-ui/react'

import Home from "./pages/Home"
import Best from "./pages/Best"
import About from "./pages/About"

import Header from './components/Header'
import Footer from './components/Footer'

import './App.css'

class App extends Component {

  render() {
    return (
      <ChakraProvider>
        <BrowserRouter>

            <Routes>
              <Route exact path="/" element={<div><Header /><Home /><Footer /></div>} />
              <Route path="/about" element={<div><Header /><About /><Footer /></div>} />
              <Route path="/best" element={<Best />} />
            </Routes>

        </BrowserRouter>
      </ChakraProvider>
    )
  }
}

export default App;

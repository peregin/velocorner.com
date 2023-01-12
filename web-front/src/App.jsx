import React from 'react';
import {

  HashRouter, Route, Routes,
} from 'react-router-dom';

import { ChakraProvider } from '@chakra-ui/react'
//import { OAuthPopup } from '@tasoskakour/react-use-oauth2'
import OAuth2Popup from './components/OAuth2Popup.tsx'

import Home from "./pages/Home"
import Best from "./pages/Best"
import About from "./pages/About"

import Header from './components/Header'
import Footer from './components/Footer'


import './App.css'

const App = () => {

  return (
    <ChakraProvider>
      <HashRouter>
        <Routes>
          <Route exact path="/" element={<div><Header /><Home /><Footer /></div>} />
          <Route exact path="/best" element={<Best />} />
          <Route path="/about" element={<div><Header /><About /><Footer /></div>} />
          <Route exact path="/oauth/strava" element={<OAuth2Popup />} />
        </Routes>
      </HashRouter>
    </ChakraProvider>
  )
}

export default App;

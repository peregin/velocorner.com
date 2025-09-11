import React from 'react';
import { BrowserRouter, Route, Routes } from 'react-router-dom';
import OAuth2Popup from './components/OAuth2Popup.tsx'

import Home from "./pages/Home"
import Best from "./pages/Best"
import About from "./pages/About"
import Search from "./pages/Search"
import Privacy from "./pages/Privacy"
import NotFound from "./pages/NotFound"

import Header from './components/Header'
import Footer from './components/Footer'
import { Toaster } from '@/components/ui/toaster'

import './App.css'
import Ping from './components/Ping';

const App = () => {

  return (
    <BrowserRouter future={{
      v7_startTransition: true,
      v7_relativeSplatPath: true,
    }}>
      <Routes>
        <Route exact path="/" element={<div><Header /><Home /><Footer /></div>} />
        <Route exact path="/best" element={<div><Header /><Best /><Footer /></div>} />
        <Route path="/about" element={<div><Header /><About /><Footer /></div>} />
        <Route exact path="/search" element={<div><Header /><Search /><Footer /></div>} />
        <Route exact path="/privacy" element={<div><Header /><Privacy /><Footer /></div>} />
        <Route exact path="/oauth/strava" element={<OAuth2Popup />} />
        <Route exact path="/health" element={<Ping />} />
        <Route path="*" element={<div><Header /><NotFound /><Footer /></div>} />
      </Routes>
      <Toaster />
    </BrowserRouter>
  )
}

export default App;

import { BrowserRouter, Route, Routes } from 'react-router-dom';
import { useState, useEffect, useRef } from 'react';
import OAuth2Popup from './components/OAuth2Popup.js'

import { useBreakpointValue, Flex, Box, Container, HStack, Image, IconButton, Text, Button, Icon } from '@chakra-ui/react'

import Home from "./pages/Home"
import About from "./pages/About"
import Search from "./pages/Search"
import Privacy from "./pages/Privacy"
import NotFound from "./pages/NotFound"

import Header from './components/Header'
import Hero from './components/Hero'
import { Toaster } from '@/components/ui/toaster'

import './App.css'
import Ping from './components/Ping';
import Stats from './components/Stats.js';
import DemoCharts from './components/DemoCharts.js';
import Features from './components/Features.js';
import Footer2 from './components/Footer2.js';

// ReactGA.initialize([
//   { trackingId: 'G-7B41YC11PS' }
// ]);

// const Layout = ({ children }) => {
//   const { user } = useAuthContext();
//   const userId = user?.data?.id;
//   //console.log(`setting userId ${userId}`);
//   ReactGA.set({ userId: userId });
//   return (
//     <>
//       <Header />
//       {children}
//     </>
//   );
// }

const App = () => {

  const [isMenuOpen, setIsMenuOpen] = useState(false);

  return (
    <Box minH="100vh" bgGradient="to-br" gradientFrom="gray.50" gradientVia="white" gradientTo="blue.50">
      <Hero isMenuOpen={isMenuOpen} setIsMenuOpen={setIsMenuOpen} />
      <Stats />
      <DemoCharts />
      <Features />
      <Footer2/>
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<div><Header /><Home /><Footer2 /></div>} />
          <Route path="/about" element={<div><Header /><About /><Footer2 /></div>} />
          <Route path="/search" element={<div><Header /><Search /><Footer2 /></div>} />
          <Route path="/privacy" element={<div><Header /><Privacy /><Footer2 /></div>} />
          <Route path="/oauth/strava" element={<OAuth2Popup />} />
          <Route path="/health" element={<Ping />} />
          <Route path="*" element={<div><Header /><NotFound /><Footer2 /></div>} />
        </Routes>
        <Toaster />
      </BrowserRouter>
    </Box>
  )
}

export default App;

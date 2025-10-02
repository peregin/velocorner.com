import { BrowserRouter, Route, Routes } from 'react-router-dom';
import { useState, useEffect, useRef, ReactNode } from 'react';
import OAuth2Popup from './components/OAuth2Popup.js'

import ReactGA from 'react-ga4';

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

ReactGA.initialize([
  { trackingId: 'G-7B41YC11PS' }
]);

const Layout = ({ children }: { children: ReactNode }) => {
  // const { user } = useAuthContext();
  // const userId = user?.data?.id;
  //console.log(`setting userId ${userId}`);
  // ReactGA.set({ userId: userId });

  const [isMenuOpen, setIsMenuOpen] = useState(false);

  return (
    <Box minH="100vh" bgGradient="to-br" gradientFrom="gray.50" gradientVia="white" gradientTo="blue.50">
      <Header />
      <Hero isMenuOpen={isMenuOpen} setIsMenuOpen={setIsMenuOpen} />
      <Stats />
      <DemoCharts />
      <Features />
      {children}
      <Footer2 />
      <Toaster />
    </Box>
  );
}

const App = () => {

  useEffect(() => {
    const page = window.location.pathname + window.location.search;
    //console.log(`sending pageview for ${page}`);
    ReactGA.send({ hitType: "pageview", page: page });
  }, []);

  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Layout><Home /></Layout>} />
        <Route path="/about" element={<Layout><About /></Layout>} />
        <Route path="/search" element={<Layout><Search /></Layout>} />
        <Route path="/privacy" element={<Layout><Privacy /></Layout>} />
        <Route path="/oauth/strava" element={<OAuth2Popup />} />
        <Route path="/health" element={<Ping />} />
        <Route path="*" element={<Layout><NotFound /></Layout>} />
      </Routes>
    </BrowserRouter>
  )
}

export default App;

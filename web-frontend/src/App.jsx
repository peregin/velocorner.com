import { BrowserRouter, Route, Routes } from 'react-router-dom';
import { useState, useEffect, useRef } from 'react';
import OAuth2Popup from './components/OAuth2Popup.tsx'

import { useBreakpointValue, Flex, Box, Container, HStack, Image, IconButton, Text, Button, Icon } from '@chakra-ui/react'

import Home from "./pages/Home"
import About from "./pages/About"
import Search from "./pages/Search"
import Privacy from "./pages/Privacy"
import NotFound from "./pages/NotFound"

import Header from './components/Header'
import Footer from './components/Footer'
import Hero from './components/Hero'
import { Toaster } from '@/components/ui/toaster'

import './App.css'
import Ping from './components/Ping';
import Stats from './components/Stats';
import DemoCharts from './components/DemoCharts';
import Features from './components/Features';
import Footer2 from './components/Footer2';

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
      <BrowserRouter future={{
        v7_startTransition: true,
        v7_relativeSplatPath: true,
      }}>
        <Routes>
          <Route exact path="/" element={<div><Header /><Home /><Footer /></div>} />
          <Route path="/about" element={<div><Header /><About /><Footer /></div>} />
          <Route exact path="/search" element={<div><Header /><Search /><Footer /></div>} />
          <Route exact path="/privacy" element={<div><Header /><Privacy /><Footer /></div>} />
          <Route exact path="/oauth/strava" element={<OAuth2Popup />} />
          <Route exact path="/health" element={<Ping />} />
          <Route path="*" element={<div><Header /><NotFound /><Footer /></div>} />
        </Routes>
        <Toaster />
      </BrowserRouter>
    </Box>
  )
}

export default App;

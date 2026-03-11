import { BrowserRouter, Route, Routes } from 'react-router-dom';
import { useState, useEffect, ReactNode } from 'react';
import OAuth2Popup from './components/OAuth2Popup.js'

import ReactGA from 'react-ga4';

import { Box } from '@chakra-ui/react'

import Home from "./pages/Home"
import About from "./pages/About"
import Search from "./pages/Search"
import Privacy from "./pages/Privacy"
import NotFound from "./pages/NotFound"
import Admin from "./pages/Admin"

import Header from './components/Header'
import { Toaster } from '@/components/ui/toaster'

import './App.css'
import Health from './pages/Health.js';
import Footer from './components/Footer.js';

ReactGA.initialize([
  { trackingId: 'G-1GNXX7WZHH' }
]);

const Layout = ({ children }: { children: ReactNode }) => {
  // const { user } = useAuthContext();
  // const userId = user?.data?.id;
  //console.log(`setting userId ${userId}`);
  // ReactGA.set({ userId: userId });

  const [isMenuOpen, setIsMenuOpen] = useState(false);

  return (
    <Box minH="100vh" position="relative" overflow="clip">
      <Box
        position="absolute"
        insetX="0"
        top="-12rem"
        h="28rem"
        bgGradient="radial(circle, rgba(44,152,240,0.2) 0%, rgba(44,152,240,0) 68%)"
        pointerEvents="none"
      />
      <Box
        position="absolute"
        right="-8rem"
        top="18rem"
        h="22rem"
        w="22rem"
        borderRadius="full"
        bg="rgba(23, 166, 133, 0.12)"
        filter="blur(80px)"
        pointerEvents="none"
      />
      <Header isMenuOpen={isMenuOpen} setIsMenuOpen={setIsMenuOpen}/>
      {children}
      <Footer />
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
        <Route path="/admin" element={<Layout><Admin /></Layout>} />
        <Route path="/privacy" element={<Layout><Privacy /></Layout>} />
        <Route path="/oauth/strava" element={<OAuth2Popup />} />
        <Route path="/health" element={<Health />} />
        <Route path="*" element={<Layout><NotFound /></Layout>} />
      </Routes>
    </BrowserRouter>
  )
}

export default App;

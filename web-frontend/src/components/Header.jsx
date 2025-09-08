import React from 'react';
import { Link } from 'react-router-dom';
import { SearchIcon } from "@chakra-ui/icons";
import { useBreakpointValue, useColorModeValue, Flex, Box, Container, HStack, Image, IconButton, Text, Button } from '@chakra-ui/react'

const Header = () => {

  const isDesktop = useBreakpointValue({ base: false, lg: true })
  return (
    <Box as="section" pb={{ base: '12', md: '24' }}>
      <Box as="nav" bg="bg-surface" boxShadow={useColorModeValue('sm', 'sm-dark')}>
        <Container py={{ base: '4', lg: '5' }}>
          <HStack spacing='5' justify="space-between">
            <HStack spacing={4}>
              <Image src='/images/logo50.png' alt="Velocorner Logo"/>
              <Text fontSize='1.5em' fontWeight='semibold'>Velocorner</Text>
            </HStack>
            
            {isDesktop ? (
              <Flex justify="space-between" flex="1" align="center">
                <HStack spacing={6}>
                  <Link to="/">
                    <Button variant="ghost" size="sm">Home</Button>
                  </Link>
                  <Link to="/search">
                    <Button variant="ghost" size="sm">Search</Button>
                  </Link>
                  <Link to="/brands">
                    <Button variant="ghost" size="sm">Brands</Button>
                  </Link>
                  <Link to="/best">
                    <Button variant="ghost" size="sm">Best Prices</Button>
                  </Link>
                  <Link to="/about">
                    <Button variant="ghost" size="sm">About</Button>
                  </Link>
                  <Link to="/privacy">
                    <Button variant="ghost" size="sm">Privacy</Button>
                  </Link>
                </HStack>
              </Flex>
              
            ) : (
              <IconButton
                variant="ghost"
                icon={<SearchIcon />}
                aria-label="Open Menu"
              />
            )}
          </HStack>
        </Container>
      </Box>
    </Box>
    )
}

export default Header;
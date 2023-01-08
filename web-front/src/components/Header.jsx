import React from 'react';
import { Link } from 'react-router-dom';
import { SearchIcon } from "@chakra-ui/icons";
import { useBreakpointValue, useColorModeValue, Flex, Box, Container, HStack, Image, IconButton, Text } from '@chakra-ui/react'

const Header = () => {

  const isDesktop = useBreakpointValue({ base: false, lg: true })
  return (
    <Box as="section" pb={{ base: '12', md: '24' }}>
      <Box as="nav" bg="bg-surface" boxShadow={useColorModeValue('sm', 'sm-dark')}>
        <Container py={{ base: '4', lg: '5' }}>
          <HStack spacing='5' justify="space-between">
            <Image src='/images/logo50.png'/>
            <Text pr='4em' fontSize='1.5em' fontWeight='semibold'>Velocorner</Text>
            {isDesktop ? (
              <Flex justify="space-between" flex="1">
                <Link to="/">Home</Link>
                <Link to="/best">Best prices</Link>
                <Link to="/about">About</Link>
                <IconButton
                    variant="ghost"
                    icon={<SearchIcon />}
                    aria-label="Open Menu"
                />
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
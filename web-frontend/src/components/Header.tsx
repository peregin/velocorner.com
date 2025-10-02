import { VStack, Flex, Box, Container, HStack, Image, IconButton, Text, Button, Link } from '@chakra-ui/react'
import { LuMenu, LuX } from 'react-icons/lu';

interface HeaderProps {
  isMenuOpen: boolean;
  setIsMenuOpen: (value: boolean) => void;
}

const Header = ({ isMenuOpen, setIsMenuOpen }: HeaderProps) => {

  return (
    <Box as="nav" py={6}>
      <Container maxW="container.xl">
        <Flex alignItems="center" justifyContent="space-between">
          <HStack gap={4}>
            <Image boxSize="40px" src='/images/logo50.png' alt="Velocorner Logo" />
            <Text fontSize='1.5em' fontWeight='semibold' textTransform='uppercase'>Velocorner</Text>
          </HStack>

          <HStack gap={8} display={{ base: 'none', md: 'flex' }}>
            {/* <Link href="#features" color="gray.700" _hover={{ color: 'brand.600' }} fontWeight="medium">
              Features
            </Link> */}
            <Link href="#stats" color="gray.700" _hover={{ color: 'brand.600' }} fontWeight="medium">
              Statistics
            </Link>
            <Link href="#about" color="gray.700" _hover={{ color: 'brand.600' }} fontWeight="medium">
              About
            </Link>
            <Button
              //bgGradient="to-r"
              gradientFrom="brand.600"
              gradientTo="cyan.600"
              //color="white"
              fontWeight="medium"
              _hover={{ boxShadow: 'lg', transform: 'scale(1.05)' }}
              transition="all 0.2s"
            >
              Connect Strava
            </Button>
          </HStack>

          <IconButton
            display={{ base: 'flex', md: 'none' }}
            onClick={() => setIsMenuOpen(!isMenuOpen)}
            aria-label="Toggle menu"
            variant="ghost"
            color="gray.700"
            _hover={{ color: 'brand.600' }}
          >
            {isMenuOpen ? <LuX /> : <LuMenu />}
          </IconButton>
        </Flex>

        {isMenuOpen && (
          <VStack
            display={{ base: 'flex', md: 'none' }}
            mt={6}
            pb={6}
            gap={4}
            borderTop="1px"
            borderColor="gray.200"
            pt={6}
            alignItems="stretch"
          >
            <Link href="#features" color="gray.700" _hover={{ color: 'brand.600' }} fontWeight="medium">
              Features
            </Link>
            <Link href="#stats" color="gray.700" _hover={{ color: 'brand.600' }} fontWeight="medium">
              Statistics
            </Link>
            <Link href="#about" color="gray.700" _hover={{ color: 'brand.600' }} fontWeight="medium">
              About
            </Link>
            <Button
              bgGradient="to-r"
              gradientFrom="brand.600"
              gradientTo="cyan.600"
              color="gray.700"
              fontWeight="medium"
            >
              Connect Strava
            </Button>
          </VStack>
        )}
      </Container>
    </Box>
  )
}

export default Header;
